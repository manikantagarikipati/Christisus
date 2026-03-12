import { useState } from 'react';
import {
  StudentInfoViewData,
  ClassInfo,
  createClassInfo,
  Profile,
  Language,
  Student,
} from '../types/models';
import { ManiClassPlanner } from '../planner/ManiClassPlanner';
import { writeResultsToExcel, downloadBlob } from '../utils/excelUtils';

interface Props {
  students: StudentInfoViewData[];
  onBack: () => void;
}

const PROFILE_OPTIONS_1 = ['', 'Normal', 'Bilingual', 'Musik'];
const PROFILE_OPTIONS_2 = ['', 'Normal', 'Bilingual', 'Musik'];
const PROFILE_OPTIONS_3 = ['', 'Normal', 'Bilingual', 'Musik'];
const LANGUAGE_OPTIONS = ['', 'French', 'Latin', 'Both'];

export function SelectClassCombination({ students, onBack }: Props) {
  const [classCount, setClassCount] = useState('');
  const [studentsPerClass, setStudentsPerClass] = useState('');
  const [classList, setClassList] = useState<ClassInfo[]>([]);
  const [generatedFileName, setGeneratedFileName] = useState<string | null>(null);

  function handleContinue() {
    if (!classCount || !studentsPerClass) {
      alert('Please fill all the fields');
      return;
    }

    const count = parseInt(classCount);
    const perClass = parseInt(studentsPerClass);

    if (count * perClass < students.length) {
      alert('Please enter more students per class');
      return;
    }

    const newClassList: ClassInfo[] = [];
    for (let i = 0; i < count; i++) {
      newClassList.push(createClassInfo(`Class ${i + 1}`));
    }
    setClassList(newClassList);
  }

  function updateClassInfo(index: number, field: keyof ClassInfo, value: string) {
    setClassList((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  }

  function handleCreateClasses() {
    if (classList.length === 0) {
      alert('Please configure classes first');
      return;
    }

    const fileName = prompt('Enter file name:', 'class_assignments');
    if (!fileName) return;

    const allowedProfileCombinations: Profile[][] = [];
    const allowedLanguageCombinations: Language[][] = [];

    for (const classInfo of classList) {
      const profileCombo: Profile[] = [];

      if (classInfo.profile1) {
        const p = mapProfile(classInfo.profile1);
        if (p) profileCombo.push(p);
      }
      if (classInfo.profile2) {
        const p = mapProfile(classInfo.profile2);
        if (p) profileCombo.push(p);
      }
      if (classInfo.profile3) {
        const p = mapProfile(classInfo.profile3);
        if (p) profileCombo.push(p);
      }

      const languageCombo: Language[] =
        classInfo.language === 'French'
          ? [Language.F]
          : classInfo.language === 'Latin'
            ? [Language.L]
            : [Language.F, Language.L];

      allowedProfileCombinations.push(profileCombo);
      allowedLanguageCombinations.push(languageCombo);
    }

    const planner = new ManiClassPlanner(
      parseInt(classCount),
      parseInt(studentsPerClass),
      allowedProfileCombinations,
      allowedLanguageCombinations
    );

    planner.assignStudentsToClasses(convertToStudents(students));
    planner.optimizeClassAssignments();

    const result = planner.getResult();
    const blob = writeResultsToExcel(result.classes, result.unassignedStudents, fileName);
    downloadBlob(blob, `${fileName}.xlsx`);
    setGeneratedFileName(fileName);

    alert('Classes Created Successfully');
  }

  return (
    <div className="select-class-combination">
      <div className="header">
        <h1>Class Configuration</h1>
        <button className="btn btn-secondary" onClick={onBack}>
          Back
        </button>
      </div>

      <div className="config-form">
        <div className="form-group">
          <label>Number of Classes</label>
          <input
            type="number"
            value={classCount}
            onChange={(e) => setClassCount(e.target.value)}
            min="1"
          />
        </div>
        <div className="form-group">
          <label>Students per Class</label>
          <input
            type="number"
            value={studentsPerClass}
            onChange={(e) => setStudentsPerClass(e.target.value)}
            min="1"
          />
        </div>
        <button className="btn btn-primary" onClick={handleContinue}>
          Configure Classes
        </button>
      </div>

      {classList.length > 0 && (
        <>
          <div className="class-list">
            <table>
              <thead>
                <tr>
                  <th>Class Name</th>
                  <th>Profile 1</th>
                  <th>Profile 2</th>
                  <th>Profile 3</th>
                  <th>Language</th>
                </tr>
              </thead>
              <tbody>
                {classList.map((classInfo, index) => (
                  <tr key={index}>
                    <td>{classInfo.name}</td>
                    <td>
                      <select
                        value={classInfo.profile1}
                        onChange={(e) => updateClassInfo(index, 'profile1', e.target.value)}
                      >
                        {PROFILE_OPTIONS_1.map((opt) => (
                          <option key={opt} value={opt}>
                            {opt || '-- Select --'}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.profile2}
                        onChange={(e) => updateClassInfo(index, 'profile2', e.target.value)}
                      >
                        {PROFILE_OPTIONS_2.map((opt) => (
                          <option key={opt} value={opt}>
                            {opt || '-- Select --'}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.profile3}
                        onChange={(e) => updateClassInfo(index, 'profile3', e.target.value)}
                      >
                        {PROFILE_OPTIONS_3.map((opt) => (
                          <option key={opt} value={opt}>
                            {opt || '-- Select --'}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.language}
                        onChange={(e) => updateClassInfo(index, 'language', e.target.value)}
                      >
                        {LANGUAGE_OPTIONS.map((opt) => (
                          <option key={opt} value={opt}>
                            {opt || '-- Select --'}
                          </option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="actions">
            <button className="btn btn-success" onClick={handleCreateClasses}>
              Create Classes
            </button>
          </div>
        </>
      )}

      {generatedFileName && (
        <div className="success-message">
          File "{generatedFileName}.xlsx" has been downloaded.
        </div>
      )}
    </div>
  );
}

function mapProfile(profileStr: string): Profile | null {
  switch (profileStr) {
    case 'Normal':
      return Profile.N;
    case 'Bilingual':
      return Profile.B;
    case 'Musik':
      return Profile.M;
    default:
      return null;
  }
}

function convertToStudents(viewData: StudentInfoViewData[]): Student[] {
  return viewData.map((s) => ({
    lastName: s.name,
    firstName: s.firstName,
    profile:
      s.profile === 'B' ? Profile.B : s.profile === 'M' ? Profile.M : Profile.N,
    language: s.language === 'F' ? Language.F : Language.L,
    friendsList: [s.friend1, s.friend2].filter((f) => f !== ''),
    nonFriendsList: [s.unFriend1, s.unFriend2].filter((f) => f !== ''),
  }));
}
