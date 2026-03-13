import * as XLSX from 'xlsx';
import { StudentInfoViewData, ClassRoom, Student } from '../types/models';

export function readExcelFile(file: File): Promise<StudentInfoViewData[]> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: 'array' });
        const sheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[sheetName];
        const rows: string[][] = XLSX.utils.sheet_to_json(sheet, { header: 1 });

        const students: StudentInfoViewData[] = [];
        let emptyRowCount = 0;
        const emptyRowThreshold = 30;

        for (let i = 1; i < rows.length; i++) {
          const row = rows[i];
          if (!row || row.length === 0 || !row[0] || row[0].toString().trim() === '') {
            emptyRowCount++;
            if (emptyRowCount >= emptyRowThreshold) break;
            continue;
          }
          emptyRowCount = 0;

          students.push({
            name: row[0]?.toString() || '',
            firstName: row[1]?.toString() || '',
            profile: row[2]?.toString() || '',
            language: row[3]?.toString() || '',
            unFriend1: row[4]?.toString() || '',
            unFriend2: row[5]?.toString() || '',
            friend1: row[6]?.toString() || '',
            friend2: row[7]?.toString() || '',
          });
        }

        students.sort((a, b) => a.name.localeCompare(b.name));
        resolve(students);
      } catch (error) {
        reject(error);
      }
    };
    reader.onerror = reject;
    reader.readAsArrayBuffer(file);
  });
}

export function createMasterFile(students: StudentInfoViewData[]): Blob {
  const wsData = [
    ['Name', 'First Name', 'Profile', 'Language', 'UnFriend1', 'UnFriend2', 'Friend1', 'Friend2'],
    ...students.map((s) => [
      s.name,
      s.firstName,
      s.profile,
      s.language,
      s.unFriend1,
      s.unFriend2,
      s.friend1,
      s.friend2,
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(wsData);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Master List');

  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
  return new Blob([wbout], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
}

export function writeResultsToExcel(
  classes: ClassRoom[],
  conflictedStudents: Student[],
  _fileName: string
): Blob {
  const wb = XLSX.utils.book_new();

  for (const classRoom of classes) {
    const wsData = [
      ['Last Name', 'First Name', 'Profile', 'Language', 'Friends', 'Non-Friends'],
      ...classRoom.students.map((s) => [
        s.lastName,
        s.firstName,
        s.profile,
        s.language,
        s.friendsList.join(', '),
        s.nonFriendsList.join(', '),
      ]),
    ];

    const ws = XLSX.utils.aoa_to_sheet(wsData);
    const sheetName = `Class ${classRoom.id}`;
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
  }

  if (conflictedStudents.length > 0) {
    const conflictData = [
      ['Last Name', 'First Name', 'Profile', 'Language', 'Friends', 'Non-Friends'],
      ...conflictedStudents.map((s) => [
        s.lastName,
        s.firstName,
        s.profile,
        s.language,
        s.friendsList.join(', '),
        s.nonFriendsList.join(', '),
      ]),
    ];

    const conflictWs = XLSX.utils.aoa_to_sheet(conflictData);
    XLSX.utils.book_append_sheet(wb, conflictWs, 'Conflicts');
  }

  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
  return new Blob([wbout], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
}

export function downloadBlob(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
