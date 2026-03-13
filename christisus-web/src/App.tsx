import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StudentInfoViewData } from './types/models';
import { FillStudentInfo } from './components/FillStudentInfo';
import { SelectClassCombination } from './components/SelectClassCombination';
import './App.css';

type Screen = 'fill-student' | 'select-class';

export default function App() {
  const { t, i18n } = useTranslation();
  const [currentScreen, setCurrentScreen] = useState<Screen>('fill-student');
  const [students, setStudents] = useState<StudentInfoViewData[]>([]);

  function handleContinue(studentData: StudentInfoViewData[]) {
    setStudents(studentData);
    setCurrentScreen('select-class');
  }

  function handleBack() {
    setCurrentScreen('fill-student');
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>{t('app.title')}</h1>
        <div className="lang-switcher">
          <button
            className={`lang-btn ${i18n.language === 'en' ? 'active' : ''}`}
            onClick={() => { i18n.changeLanguage('en'); localStorage.setItem('lang', 'en'); }}
          >
            EN
          </button>
          <button
            className={`lang-btn ${i18n.language === 'de' ? 'active' : ''}`}
            onClick={() => { i18n.changeLanguage('de'); localStorage.setItem('lang', 'de'); }}
          >
            DE
          </button>
        </div>
      </header>
      <main className="app-main">
        {currentScreen === 'fill-student' && (
          <FillStudentInfo onContinue={handleContinue} />
        )}
        {currentScreen === 'select-class' && (
          <SelectClassCombination students={students} onBack={handleBack} />
        )}
      </main>
    </div>
  );
}
