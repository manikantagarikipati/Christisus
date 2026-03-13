import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { StudentInfoViewData } from '../types/models';
import { readExcelFile, createMasterFile, downloadBlob } from '../utils/excelUtils';

interface Props {
  onContinue: (students: StudentInfoViewData[]) => void;
}

export function FillStudentInfo({ onContinue }: Props) {
  const { t } = useTranslation();
  const [students, setStudents] = useState<StudentInfoViewData[]>([]);
  const [loading, setLoading] = useState(false);

  const studentNames = useMemo(
    () => students.map((s) => `${s.name} ${s.firstName}`),
    [students]
  );

  async function handleFileSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    setLoading(true);
    try {
      const data = await readExcelFile(file);
      setStudents(data);
    } catch (error) {
      alert(t('errors.excelReadError'));
      console.error(error);
    } finally {
      setLoading(false);
    }
  }

  function updateStudent(index: number, field: keyof StudentInfoViewData, value: string) {
    setStudents((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  }

  function handleSave(createFile: boolean) {
    if (students.length === 0) {
      alert(t('errors.selectFileFirst'));
      return;
    }

    if (createFile) {
      const blob = createMasterFile(students);
      downloadBlob(blob, 'master_list.xlsx');
    }

    onContinue(students);
  }

  return (
    <div className="fill-student-info">
      <div className="header">
        <h1>{t('studentInfo.title')}</h1>
        <div className="actions">
          <label className="btn btn-primary">
            {t('studentInfo.selectExcel')}
            <input
              type="file"
              accept=".xlsx,.xls"
              onChange={handleFileSelect}
              style={{ display: 'none' }}
            />
          </label>
          <button className="btn btn-success" onClick={() => handleSave(true)}>
            {t('studentInfo.saveAndCreate')}
          </button>
          <button className="btn btn-secondary" onClick={() => handleSave(false)}>
            {t('studentInfo.continueWithout')}
          </button>
        </div>
      </div>

      {loading && <div className="loading">{t('studentInfo.loading')}</div>}

      {students.length > 0 && (
        <div className="student-list">
          <table>
            <thead>
              <tr>
                <th>{t('studentInfo.name')}</th>
                <th>{t('studentInfo.firstName')}</th>
                <th>{t('studentInfo.profile')}</th>
                <th>{t('studentInfo.language')}</th>
                <th>{t('studentInfo.friend1')}</th>
                <th>{t('studentInfo.friend2')}</th>
                <th>{t('studentInfo.unFriend1')}</th>
                <th>{t('studentInfo.unFriend2')}</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student, index) => (
                <tr key={index}>
                  <td>{student.name}</td>
                  <td>{student.firstName}</td>
                  <td>{student.profile}</td>
                  <td>{student.language}</td>
                  <td>
                    <AutocompleteInput
                      value={student.friend1}
                      options={studentNames}
                      onChange={(val) => updateStudent(index, 'friend1', val)}
                      onClear={() => updateStudent(index, 'friend1', '')}
                    />
                  </td>
                  <td>
                    <AutocompleteInput
                      value={student.friend2}
                      options={studentNames}
                      onChange={(val) => updateStudent(index, 'friend2', val)}
                      onClear={() => updateStudent(index, 'friend2', '')}
                    />
                  </td>
                  <td>
                    <AutocompleteInput
                      value={student.unFriend1}
                      options={studentNames}
                      onChange={(val) => updateStudent(index, 'unFriend1', val)}
                      onClear={() => updateStudent(index, 'unFriend1', '')}
                    />
                  </td>
                  <td>
                    <AutocompleteInput
                      value={student.unFriend2}
                      options={studentNames}
                      onChange={(val) => updateStudent(index, 'unFriend2', val)}
                      onClear={() => updateStudent(index, 'unFriend2', '')}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

interface AutocompleteInputProps {
  value: string;
  options: string[];
  onChange: (value: string) => void;
  onClear: () => void;
}

function AutocompleteInput({ value, options, onChange, onClear }: AutocompleteInputProps) {
  const [showDropdown, setShowDropdown] = useState(false);
  const [filter, setFilter] = useState('');

  const filteredOptions = options.filter((opt) =>
    opt.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div className="autocomplete">
      <input
        type="text"
        value={value}
        onChange={(e) => {
          setFilter(e.target.value);
          onChange(e.target.value);
        }}
        onFocus={() => setShowDropdown(true)}
        onBlur={() => setTimeout(() => setShowDropdown(false), 200)}
      />
      {value && (
        <button className="clear-btn" onClick={onClear}>
          ×
        </button>
      )}
      {showDropdown && filteredOptions.length > 0 && (
        <ul className="dropdown">
          {filteredOptions.slice(0, 10).map((opt, i) => (
            <li
              key={i}
              onMouseDown={() => {
                onChange(opt);
                setShowDropdown(false);
              }}
            >
              {opt}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
