package com.example.studentportal.data

import android.os.Parcel
import android.os.Parcelable

class lesson(
    val id: String,            // уникальный идентификатор
    val type: String,       // тип пары
    val title: String,      // название пары
    val number: String,    // номер пары
    val time: String,      // время пары
    val audience: String,  // аудитория
    val teacher: String,   // преподаватель
    val typeOfTest: String, // тип аттестации
    val building: String,   // корпус
    val adress: String,        // адрес
    val dayOfWeek: Int,         // День недели: 1 (понедельник) - 7 (воскресенье)
    val weekType: String)       // Тип недели: "верхняя", "нижняя", "обе"

//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString()!!
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(type)
//        parcel.writeString(title)
//        parcel.writeString(number)
//        parcel.writeString(time)
//        parcel.writeString(audience)
//        parcel.writeString(teacher)
//        parcel.writeString(typeOfTest)
//        parcel.writeString(building)
//        parcel.writeString(adress)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<lesson> {
//        override fun createFromParcel(parcel: Parcel): lesson {
//            return lesson(parcel)
//        }
//
//        override fun newArray(size: Int): Array<lesson?> {
//            return arrayOfNulls(size)
//        }
//    }
//}