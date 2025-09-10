package io.composeflow.materialicons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.ExitToApp
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.twotone.List
import androidx.compose.material.icons.automirrored.twotone.Send
import androidx.compose.material.icons.twotone.AccountBox
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.AddCircle
import androidx.compose.material.icons.twotone.ArrowDropDown
import androidx.compose.material.icons.twotone.Build
import androidx.compose.material.icons.twotone.Call
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Create
import androidx.compose.material.icons.twotone.DateRange
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.FavoriteBorder
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material.icons.twotone.KeyboardArrowUp
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.MailOutline
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material.icons.twotone.Notifications
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Phone
import androidx.compose.material.icons.twotone.Place
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material.icons.twotone.ShoppingCart
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material.icons.twotone.ThumbUp
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object TwoToneSerializer : FallbackEnumSerializer<TwoTone>(TwoTone::class, TwoTone.entries.toTypedArray())

/**
 * Enum of TwoTone icons from material-icons-core
 */
@Serializable(TwoToneSerializer::class)
@SerialName("TwoTone")
enum class TwoTone(
    @Transient
    override val imageVector: ImageVector,
    @Transient
    override val packageDescriptor: String = "twotone",
    @Transient
    override val memberDescriptor: String = "TwoTone",
) : ImageVectorHolder {
    AccountBox(Icons.TwoTone.AccountBox),
    AccountCircle(Icons.TwoTone.AccountCircle),
    AddCircle(Icons.TwoTone.AddCircle),
    Add(Icons.TwoTone.Add),
    ArrowBack(Icons.AutoMirrored.TwoTone.ArrowBack),
    ArrowDropDown(Icons.TwoTone.ArrowDropDown),
    ArrowForward(Icons.AutoMirrored.TwoTone.ArrowForward),
    Build(Icons.TwoTone.Build),
    Call(Icons.TwoTone.Call),
    CheckCircle(Icons.TwoTone.CheckCircle),
    Check(Icons.TwoTone.Check),
    Clear(Icons.TwoTone.Clear),
    Close(Icons.TwoTone.Close),
    Create(Icons.TwoTone.Create),
    DateRange(Icons.TwoTone.DateRange),
    Delete(Icons.TwoTone.Delete),
    Done(Icons.TwoTone.Done),
    Edit(Icons.TwoTone.Edit),
    Email(Icons.TwoTone.Email),
    ExitToApp(Icons.AutoMirrored.TwoTone.ExitToApp),
    Face(Icons.TwoTone.Face),
    FavoriteBorder(Icons.TwoTone.FavoriteBorder),
    Favorite(Icons.TwoTone.Favorite),
    Home(Icons.TwoTone.Home),
    Info(Icons.TwoTone.Info),
    KeyboardArrowDown(Icons.TwoTone.KeyboardArrowDown),
    KeyboardArrowLeft(Icons.AutoMirrored.TwoTone.KeyboardArrowLeft),
    KeyboardArrowRight(Icons.AutoMirrored.TwoTone.KeyboardArrowRight),
    KeyboardArrowUp(Icons.TwoTone.KeyboardArrowUp),
    List(Icons.AutoMirrored.TwoTone.List),
    LocationOn(Icons.TwoTone.LocationOn),
    Lock(Icons.TwoTone.Lock),
    MailOutline(Icons.TwoTone.MailOutline),
    Menu(Icons.TwoTone.Menu),
    MoreVert(Icons.TwoTone.MoreVert),
    Notifications(Icons.TwoTone.Notifications),
    Person(Icons.TwoTone.Person),
    Phone(Icons.TwoTone.Phone),
    Place(Icons.TwoTone.Place),
    PlayArrow(Icons.TwoTone.PlayArrow),
    Refresh(Icons.TwoTone.Refresh),
    Search(Icons.TwoTone.Search),
    Send(Icons.AutoMirrored.TwoTone.Send),
    Settings(Icons.TwoTone.Settings),
    Share(Icons.TwoTone.Share),
    ShoppingCart(Icons.TwoTone.ShoppingCart),
    Star(Icons.TwoTone.Star),
    ThumbUp(Icons.TwoTone.ThumbUp),
    Warning(Icons.TwoTone.Warning),
}
