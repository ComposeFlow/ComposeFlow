package io.composeflow.materialicons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object FilledSerializer : FallbackEnumSerializer<Filled>(
    Filled::class,
    Filled.entries.toTypedArray(),
)

/**
 * Enum of Filled icons from material-icons-core
 */
@SerialName("Filled")
@Serializable(FilledSerializer::class)
enum class Filled(
    @Transient
    override val imageVector: ImageVector,
    @Transient
    override val packageDescriptor: String = "filled",
    @Transient
    override val memberDescriptor: String = "Filled",
) : ImageVectorHolder {
    AccountBox(Icons.Filled.AccountBox),
    AccountCircle(Icons.Filled.AccountCircle),
    AddCircle(Icons.Filled.AddCircle),
    Add(Icons.Filled.Add),
    ArrowBack(Icons.AutoMirrored.Filled.ArrowBack),
    ArrowDropDown(Icons.Filled.ArrowDropDown),
    ArrowForward(Icons.AutoMirrored.Filled.ArrowForward),
    Build(Icons.Filled.Build),
    Call(Icons.Filled.Call),
    CheckCircle(Icons.Filled.CheckCircle),
    Check(Icons.Filled.Check),
    Clear(Icons.Filled.Clear),
    Close(Icons.Filled.Close),
    Create(Icons.Filled.Create),
    DateRange(Icons.Filled.DateRange),
    Delete(Icons.Filled.Delete),
    Done(Icons.Filled.Done),
    Edit(Icons.Filled.Edit),
    Email(Icons.Filled.Email),
    ExitToApp(Icons.AutoMirrored.Filled.ExitToApp),
    Face(Icons.Filled.Face),
    FavoriteBorder(Icons.Filled.FavoriteBorder),
    Favorite(Icons.Filled.Favorite),
    Home(Icons.Filled.Home),
    Info(Icons.Filled.Info),
    KeyboardArrowDown(Icons.Filled.KeyboardArrowDown),
    KeyboardArrowLeft(Icons.AutoMirrored.Filled.KeyboardArrowLeft),
    KeyboardArrowRight(Icons.AutoMirrored.Filled.KeyboardArrowRight),
    KeyboardArrowUp(Icons.Filled.KeyboardArrowUp),
    List(Icons.AutoMirrored.Filled.List),
    LocationOn(Icons.Filled.LocationOn),
    Lock(Icons.Filled.Lock),
    MailOutline(Icons.Filled.MailOutline),
    Menu(Icons.Filled.Menu),
    MoreVert(Icons.Filled.MoreVert),
    Notifications(Icons.Filled.Notifications),
    Person(Icons.Filled.Person),
    Phone(Icons.Filled.Phone),
    Place(Icons.Filled.Place),
    PlayArrow(Icons.Filled.PlayArrow),
    Refresh(Icons.Filled.Refresh),
    Search(Icons.Filled.Search),
    Send(Icons.AutoMirrored.Filled.Send),
    Settings(Icons.Filled.Settings),
    Share(Icons.Filled.Share),
    ShoppingCart(Icons.Filled.ShoppingCart),
    Star(Icons.Filled.Star),
    ThumbUp(Icons.Filled.ThumbUp),
    Warning(Icons.Filled.Warning),
}
