package lookbook

import android.os.Parcel
import android.os.Parcelable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import profile.CursorPaginationMetaData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import signin.UserResponse

interface LookBookApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>

    @GET("clothes/{category}")
    fun getClothes(
        @Header("Authorization") accessToken: String,
        @Path("category") category: String
    ): Call<List<LookBookClothesItem>>

    @GET("mannequin/me")
    fun getMannequin(
        @Header("Authorization") accessToken: String,
    ): Call<LookBookMannequin>

    @Multipart
    @POST("lookbook")
    fun uploadLookBooks(
        @Header("Authorization") accessToken: String,
        @Part("topIds") topIds: List<Int>,
        @Part("pantId") pantId: Int,
        @Part("shoeId") shoeId: Int,
        @Part("accessoryIds") accessoryIds: List<Int>,
        @Part("show") show: Boolean,
        @Part("title") title: String,
        @Part("type") type: List<String>,
        @Part("memo") memo: String,
        @Part file: MultipartBody.Part,
    ): Call<ResponseBody>

    @GET("lookbook/detail")
    fun getDetailLookBook(
        @Header("Authorization") accessToken: String,
        @Query("take") take: Int,
        @Query("cursor") cursor: Int,
        @Query("keyword") keyword: String
    ): Call<LookBookDetailResponse>
    @GET("lookbook/detail/profile/{userUUID}")
    fun getDetailProfileLookBook(
        @Header("Authorization") accessToken: String,
        @Path("userUUID") userUUID: String,
        @Query("take") take: Int,
        @Query("cursor") cursor: Int,
        @Query("keyword") keyword: String
    ): Call<LookBookDetailResponse>

    @PUT("lookbook/like/{lookbookId}")
    fun likeLookBook(
        @Header("Authorization") accessToken: String,
        @Path("lookbookId") lookbookId: Int
    ): Call<Void>

    @PUT("lookbook/clip/{lookbookId}")
    fun clipLookBook(
        @Header("Authorization") accessToken: String,
        @Path("lookbookId") lookbookId: Int
    ): Call<Void>

    @POST("comment")
    fun addComment(
        @Header("Authorization") accessToken: String,
        @Body commentRequest: CommentRequest
    ): Call<Void>
}

data class CommentRequest(
    val lookbookId: Int,
    val content: String,
    val parentCommentId: Int? = null
)

data class LookBookClothesItem(
    val id: Int,
    val url: String
)

data class LookBookMannequin(
    val sex: Int,
    val hair: Int,
    val skinColor: Int,
    val height: Int,
    val body: Int,
    val arm: Int,
    val leg: Int
)

data class LookBookDetail(
    val lookbook: LookBook,
    val user: User,
    val tops: List<Clothing>,
    val accessories: List<Clothing>,
    val pant: Clothing,
    val shoe: Clothing,
    val comments: List<Comment>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LookBook::class.java.classLoader)!!,
        parcel.readParcelable(User::class.java.classLoader)!!,
        parcel.createTypedArrayList(Clothing.CREATOR)!!,
        parcel.createTypedArrayList(Clothing.CREATOR)!!,
        parcel.readParcelable(Clothing::class.java.classLoader)!!,
        parcel.readParcelable(Clothing::class.java.classLoader)!!,
        parcel.createTypedArrayList(Comment.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lookbook, flags)
        parcel.writeParcelable(user, flags)
        parcel.writeTypedList(tops)
        parcel.writeTypedList(accessories)
        parcel.writeParcelable(pant, flags)
        parcel.writeParcelable(shoe, flags)
        parcel.writeTypedList(comments)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LookBookDetail> {
        override fun createFromParcel(parcel: Parcel): LookBookDetail {
            return LookBookDetail(parcel)
        }

        override fun newArray(size: Int): Array<LookBookDetail?> {
            return arrayOfNulls(size)
        }
    }
}

data class LookBook(
    val lookbookId: Int,
    val title: String,
    val type: List<String>,
    val memo: String,
    var likeCnt: Int,
    val commentCnt: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(lookbookId)
        parcel.writeString(title)
        parcel.writeStringList(type)
        parcel.writeString(memo)
        parcel.writeInt(likeCnt)
        parcel.writeInt(commentCnt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LookBook> {
        override fun createFromParcel(parcel: Parcel): LookBook {
            return LookBook(parcel)
        }

        override fun newArray(size: Int): Array<LookBook?> {
            return arrayOfNulls(size)
        }
    }
}

data class User(
    val uuid: String,
    val nickname: String,
    val like: Boolean,
    val save: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uuid)
        parcel.writeString(nickname)
        parcel.writeByte(if (like) 1 else 0)
        parcel.writeByte(if (save) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

data class Clothing(
    val id: Int,
    val url: String,
    val memo: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(url)
        parcel.writeString(memo)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Clothing> {
        override fun createFromParcel(parcel: Parcel): Clothing {
            return Clothing(parcel)
        }

        override fun newArray(size: Int): Array<Clothing?> {
            return arrayOfNulls(size)
        }
    }
}

data class Comment(
    val id: Int,
    val parentId: Int?,
    val content: String,
    val writer: String?,
    val writerUUID: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeValue(parentId)
        parcel.writeString(content)
        parcel.writeString(writer)
        parcel.writeString(writerUUID)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}


data class LookBookDetailResponse(
    val lookBookDetail: List<LookBookDetail>,
    val cursorPaginationMetaData: CursorPaginationMetaData
)

