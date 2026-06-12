package com.kito.testing

import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.exam.domain.model.ExamSchedule
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.friendview.domain.model.FriendScheduleItem
import com.kito.feature.gpa.domain.model.StudentProfile
import com.kito.feature.calendar.domain.model.CalendarEvent
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.schedule.domain.model.ScheduleItem

fun attendance(
    subjectCode: String = "CS101",
    subjectName: String = "Maths",
    attendedClasses: Int = 8,
    totalClasses: Int = 10,
    percentage: Double = 80.0,
    facultyName: String = "Dr. Test",
) = Attendance(subjectCode, subjectName, attendedClasses, totalClasses, percentage, facultyName)

fun examSchedule(
    subject: String = "Maths",
    date: String = "2026-06-20",
    startTime: String = "09:00:00",
    endTime: String = "11:00:00",
) = ExamSchedule(subject, null, date, "Saturday", startTime, endTime, "B1", "CS", 4)

fun faculty(id: Long = 1L, name: String = "Dr. Test") =
    Faculty(id, name, null, null)

fun scheduleItem(subject: String = "Maths", day: String = "MON") =
    ScheduleItem(subject, "09:00:00", "10:00:00", null, "CS-A", "B1")

fun studentProfile(roll: String = "22CS001", section: String = "CS-A") =
    StudentProfile(roll, section, "B1")

fun eventOrAd(id: Long = 1L, isAd: Boolean = false) =
    EventOrAd(id, "https://cdn/img.jpg", "image", "https://example.com", isAd)

fun friendScheduleItem(subject: String = "Maths", day: String = "MON") =
    FriendScheduleItem(subject, "09:00:00", "10:00:00", "101", day, "CS-A", "B1")

fun calendarEvent(id: Long = 1L, title: String = "Fest", date: String = "2026-09-01") =
    CalendarEvent(id, title, "Annual", date, "09:00", "17:00", "Cultural", null, true)
