package io.composeflow.materialicons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object RoundedSerializer : FallbackEnumSerializer<Rounded>(Rounded::class)

/**
 * Enum of rounded icons from material-icons-core
 */
@Serializable(RoundedSerializer::class)
@SerialName("Rounded")
enum class Rounded(
    @Transient
    override val imageVector: ImageVector,
    @Transient
    override val packageDescriptor: String = "rounded",
    @Transient
    override val memberDescriptor: String = "Rounded",
) : ImageVectorHolder {
    AccountBox(Icons.Rounded.AccountBox),
    AccountCircle(Icons.Rounded.AccountCircle),
    AddCircle(Icons.Rounded.AddCircle),
    Add(Icons.Rounded.Add),
    ArrowBack(Icons.AutoMirrored.Rounded.ArrowBack),
    ArrowDropDown(Icons.Rounded.ArrowDropDown),
    ArrowForward(Icons.AutoMirrored.Rounded.ArrowForward),
    Build(Icons.Rounded.Build),
    Call(Icons.Rounded.Call),
    CheckCircle(Icons.Rounded.CheckCircle),
    Check(Icons.Rounded.Check),
    Clear(Icons.Rounded.Clear),
    Close(Icons.Rounded.Close),
    Create(Icons.Rounded.Create),
    DateRange(Icons.Rounded.DateRange),
    Delete(Icons.Rounded.Delete),
    Done(Icons.Rounded.Done),
    Edit(Icons.Rounded.Edit),
    Email(Icons.Rounded.Email),
    ExitToApp(Icons.AutoMirrored.Rounded.ExitToApp),
    Face(Icons.Rounded.Face),
    FavoriteBorder(Icons.Rounded.FavoriteBorder),
    Favorite(Icons.Rounded.Favorite),
    Home(Icons.Rounded.Home),
    Info(Icons.Rounded.Info),
    KeyboardArrowDown(Icons.Rounded.KeyboardArrowDown),
    KeyboardArrowLeft(Icons.AutoMirrored.Rounded.KeyboardArrowLeft),
    KeyboardArrowRight(Icons.AutoMirrored.Rounded.KeyboardArrowRight),
    KeyboardArrowUp(Icons.Rounded.KeyboardArrowUp),
    List(Icons.AutoMirrored.Rounded.List),
    LocationOn(Icons.Rounded.LocationOn),
    Lock(Icons.Rounded.Lock),
    MailOutline(Icons.Rounded.MailOutline),
    Menu(Icons.Rounded.Menu),
    MoreVert(Icons.Rounded.MoreVert),
    Notifications(Icons.Rounded.Notifications),
    Person(Icons.Rounded.Person),
    Phone(Icons.Rounded.Phone),
    Place(Icons.Rounded.Place),
    PlayArrow(Icons.Rounded.PlayArrow),
    Refresh(Icons.Rounded.Refresh),
    Search(Icons.Rounded.Search),
    Send(Icons.AutoMirrored.Rounded.Send),
    Settings(Icons.Rounded.Settings),
    Share(Icons.Rounded.Share),
    ShoppingCart(Icons.Rounded.ShoppingCart),
    Star(Icons.Rounded.Star),
    ThumbUp(Icons.Rounded.ThumbUp),
    Warning(Icons.Rounded.Warning),
}
