export enum Profile {
  B = 'B', // Bilingual
  N = 'N', // Normal
  M = 'M', // Musik
}

export enum Language {
  F = 'F', // French
  L = 'L', // Latin
}

export interface Student {
  firstName: string;
  lastName: string;
  profile: Profile;
  language: Language;
  friendsList: string[];
  nonFriendsList: string[];
}

export interface ClassRoom {
  id: number;
  students: Student[];
  allowedProfiles: Profile[];
  allowedLanguages: Language[];
  maxStudents: number;
}

export interface StudentInfoViewData {
  name: string;
  firstName: string;
  profile: string;
  language: string;
  friend1: string;
  friend2: string;
  unFriend1: string;
  unFriend2: string;
}

export interface ClassInfo {
  name: string;
  profile1: string;
  profile2: string;
  profile3: string;
  language: string;
  profile1Position: number;
  profile2Position: number;
  profile3Position: number;
  languagePosition: number;
}

export function studentFullName(student: Student): string {
  return `${student.firstName} ${student.lastName}`;
}

export function studentReverseFullName(student: Student): string {
  return `${student.lastName} ${student.firstName}`;
}

export function createClassInfo(name: string): ClassInfo {
  return {
    name,
    profile1: '',
    profile2: '',
    profile3: '',
    language: '',
    profile1Position: -1,
    profile2Position: -1,
    profile3Position: -1,
    languagePosition: -1,
  };
}
