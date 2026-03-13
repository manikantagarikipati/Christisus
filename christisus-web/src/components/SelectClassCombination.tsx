import { useState } from 'react';
import { useTranslation } from 'react-i18next';
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

const PROFILE_KEYS = ['', 'normal', 'bilingual', 'musik'] as const;
const LANGUAGE_KEYS = ['', 'french', 'latin', 'both'] as const;

interface Props {
  students: StudentInfoViewData[];
  onBack: () => void;
}

export function SelectClassCombination({ students, onBack }: Props) {
  const { t } = useTranslation();
  const [classCount, setClassCount] = useState('');
  const [studentsPerClass, setStudentsPerClass] = useState('');
  const [classList, setClassList] = useState<ClassInfo[]>([]);
  const [generatedFileName, setGeneratedFileName] = useState<string | null>(null);

  function handleContinue() {
    if (!classCount || !studentsPerClass) {
      alert(t('errors.fillAllFields'));
      return;
    }

    const count = parseInt(classCount);
    const perClass = parseInt(studentsPerClass);

    if (count * perClass < students.length) {
      alert(t('errors.moreStudentsPerClass'));
      return;
    }

    const newClassList: ClassInfo[] = [];
    for (let i = 0; i < count; i++) {
      newClassList.push(createClassInfo(`${t('classConfig.class')} ${i + 1}`));
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
      alert(t('errors.configureFirst'));
      return;
    }

    const fileName = prompt(t('classConfig.fileNamePrompt'), 'class_assignments');
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
        classInfo.language === 'french'
          ? [Language.F]
          : classInfo.language === 'latin'
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

    alert(t('success.classesCreated'));
  }

  return (
    <div className="select-class-combination">
      <div className="header">
        <h1>{t('classConfig.title')}</h1>
        <button className="btn btn-secondary" onClick={onBack}>
          {t('classConfig.back')}
        </button>
      </div>

      <div className="config-form">
        <div className="form-group">
          <label>{t('classConfig.numberOfClasses')}</label>
          <input
            type="number"
            value={classCount}
            onChange={(e) => setClassCount(e.target.value)}
            min="1"
          />
        </div>
        <div className="form-group">
          <label>{t('classConfig.studentsPerClass')}</label>
          <input
            type="number"
            value={studentsPerClass}
            onChange={(e) => setStudentsPerClass(e.target.value)}
            min="1"
          />
        </div>
        <button className="btn btn-primary" onClick={handleContinue}>
          {t('classConfig.configureClasses')}
        </button>
      </div>

      {classList.length > 0 && (
        <>
          <div className="class-list">
            <table>
              <thead>
                <tr>
                  <th>{t('classConfig.className')}</th>
                  <th>{t('classConfig.profile1')}</th>
                  <th>{t('classConfig.profile2')}</th>
                  <th>{t('classConfig.profile3')}</th>
                  <th>{t('classConfig.language')}</th>
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
                        {PROFILE_KEYS.map((opt) => (
                          <option key={opt || 'empty'} value={opt}>
                            {opt ? t(`profile.${opt}`) : t('classConfig.selectPlaceholder')}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.profile2}
                        onChange={(e) => updateClassInfo(index, 'profile2', e.target.value)}
                      >
                        {PROFILE_KEYS.map((opt) => (
                          <option key={opt || 'empty'} value={opt}>
                            {opt ? t(`profile.${opt}`) : t('classConfig.selectPlaceholder')}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.profile3}
                        onChange={(e) => updateClassInfo(index, 'profile3', e.target.value)}
                      >
                        {PROFILE_KEYS.map((opt) => (
                          <option key={opt || 'empty'} value={opt}>
                            {opt ? t(`profile.${opt}`) : t('classConfig.selectPlaceholder')}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={classInfo.language}
                        onChange={(e) => updateClassInfo(index, 'language', e.target.value)}
                      >
                        {LANGUAGE_KEYS.map((opt) => (
                          <option key={opt || 'empty'} value={opt}>
                            {opt ? t(`lang.${opt}`) : t('classConfig.selectPlaceholder')}
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
              {t('classConfig.createClasses')}
            </button>
          </div>
        </>
      )}

      {generatedFileName && (
        <div className="success-message">
          {t('classConfig.fileDownloaded', { fileName: generatedFileName })}
        </div>
      )}
    </div>
  );
}

function mapProfile(profileStr: string): Profile | null {
  switch (profileStr) {
    case 'normal':
      return Profile.N;
    case 'bilingual':
      return Profile.B;
    case 'musik':
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
