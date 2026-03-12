import { useState } from 'react';
import { StudentInfoViewData } from './types/models';
import { FillStudentInfo } from './components/FillStudentInfo';
import { SelectClassCombination } from './components/SelectClassCombination';
import './App.css';

type Screen = 'fill-student' | 'select-class';

export default function App() {
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
        <h1>Christisus - Class Planner</h1>
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
