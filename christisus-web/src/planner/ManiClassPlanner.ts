import {
  ClassRoom,
  Language,
  Profile,
  Student,
  studentFullName,
  studentReverseFullName,
} from '../types/models';

const ITERATIONS_BUFFER = 80;
const CLASS_OPTIMISATION_ITERATIONS = 10;

export interface ClassPlannerResult {
  classes: ClassRoom[];
  unassignedStudents: Student[];
}

export class ManiClassPlanner {
  private readonly maxClasses: number;
  private readonly maxStudentsPerClass: number;
  private readonly allowedProfileCombinations: Profile[][];
  private readonly allowedLanguageCombinations: Language[][];
  private unassignedStudents: Student[] = [];
  private classes: ClassRoom[] = [];

  constructor(
    maxClasses: number,
    maxStudentsPerClass: number,
    allowedProfileCombinations: Profile[][],
    allowedLanguageCombinations: Language[][]
  ) {
    this.maxClasses = maxClasses;
    this.maxStudentsPerClass = maxStudentsPerClass;
    this.allowedProfileCombinations = allowedProfileCombinations;
    this.allowedLanguageCombinations = allowedLanguageCombinations;
  }

  assignStudentsToClasses(students: Student[]): void {
    for (let i = 0; i < this.maxClasses; i++) {
      const profileCombination =
        this.allowedProfileCombinations[i % this.allowedProfileCombinations.length];
      const languageCombination =
        this.allowedLanguageCombinations[i % this.allowedLanguageCombinations.length];
      this.classes.push({
        id: i + 1,
        students: [],
        allowedProfiles: profileCombination,
        allowedLanguages: languageCombination,
        maxStudents: this.maxStudentsPerClass,
      });
    }

    const assignedStudents = new Set<Student>();
    const studentQueue = [...students];

    studentQueue.sort((a, b) => {
      if (a.profile !== b.profile) {
        return a.profile.localeCompare(b.profile);
      }
      return a.language.localeCompare(b.language);
    });

    const maxIterations = ITERATIONS_BUFFER + students.length;
    let iteration = 0;

    while (studentQueue.length > 0 && iteration < maxIterations) {
      iteration++;
      const student = studentQueue.shift()!;

      const possibleClasses = this.classes.filter((classRoom) => {
        const hasNoNonFriend = !classRoom.students.some((s) =>
          s.nonFriendsList.includes(student.firstName)
        );
        const profileAllowed = classRoom.allowedProfiles.includes(student.profile);
        const languageAllowed = classRoom.allowedLanguages.includes(student.language);
        return hasNoNonFriend && profileAllowed && languageAllowed;
      });

      const assignedClass = possibleClasses.reduce<ClassRoom | null>((min, current) => {
        if (!min) return current;
        return current.students.length < min.students.length ? current : min;
      }, null);

      if (assignedClass) {
        if (assignedClass.students.length < this.maxStudentsPerClass) {
          assignedClass.students.push(student);
          assignedStudents.add(student);
        } else {
          const removableStudent = assignedClass.students.find(
            (s) =>
              s.friendsList.length === 0 ||
              !s.friendsList.some((friend) =>
                assignedClass.students.some((st) => st.firstName === friend)
              )
          );
          if (removableStudent) {
            const idx = assignedClass.students.indexOf(removableStudent);
            assignedClass.students.splice(idx, 1);
            assignedStudents.delete(removableStudent);
            assignedClass.students.push(student);
            assignedStudents.add(student);
            studentQueue.push(removableStudent);
          } else {
            this.unassignedStudents.push(student);
          }
        }
      } else {
        this.unassignedStudents.push(student);
      }
    }

    if (studentQueue.length > 0) {
      this.unassignedStudents.push(...studentQueue);
    }

    for (const classRoom of this.classes) {
      for (const student of [...classRoom.students]) {
        const betterClass = this.classes.find(
          (c) =>
            c.students.length < this.maxStudentsPerClass &&
            student.friendsList.some((friend) =>
              c.students.some((s) => s.firstName === friend)
            )
        );
        if (betterClass && betterClass !== classRoom) {
          const idx = classRoom.students.indexOf(student);
          classRoom.students.splice(idx, 1);
          betterClass.students.push(student);
        }
      }
    }
  }

  optimizeClassAssignments(): void {
    let count = 0;
    while (count < CLASS_OPTIMISATION_ITERATIONS) {
      this.optimiseIteration();
      count++;
    }
  }

  private optimiseIteration(): void {
    for (const classRoom of this.classes) {
      const studentsWithFriends = classRoom.students.filter(
        (s) => s.friendsList.length > 0
      );

      for (const student of studentsWithFriends) {
        const missingFriends = student.friendsList.filter(
          (friend) =>
            !classRoom.students.some(
              (s) =>
                studentFullName(s) === friend || studentReverseFullName(s) === friend
            )
        );

        if (missingFriends.length > 0) {
          const friendStudent = this.classes
            .flatMap((c) => c.students)
            .find(
              (s) =>
                missingFriends.includes(studentFullName(s)) ||
                missingFriends.includes(studentReverseFullName(s))
            );

          if (friendStudent) {
            const friendClass = this.classes.find((c) =>
              c.students.includes(friendStudent)
            );

            if (
              friendClass &&
              friendClass !== classRoom &&
              classRoom.allowedProfiles.includes(friendStudent.profile) &&
              classRoom.allowedLanguages.includes(friendStudent.language) &&
              !classRoom.students.some((s) =>
                s.nonFriendsList.includes(friendStudent.firstName)
              )
            ) {
              const swapCandidate = classRoom.students.find(
                (s) =>
                  s.friendsList.length === 0 &&
                  s.nonFriendsList.length === 0 &&
                  friendClass.allowedProfiles.includes(s.profile) &&
                  friendClass.allowedLanguages.includes(s.language)
              );

              if (swapCandidate) {
                const swapIdx = classRoom.students.indexOf(swapCandidate);
                classRoom.students.splice(swapIdx, 1);
                friendClass.students.push(swapCandidate);

                const friendIdx = friendClass.students.indexOf(friendStudent);
                friendClass.students.splice(friendIdx, 1);
                classRoom.students.push(friendStudent);
              }
            }
          }
        }
      }
    }
  }

  getResult(): ClassPlannerResult {
    return {
      classes: this.classes,
      unassignedStudents: this.unassignedStudents,
    };
  }
}
