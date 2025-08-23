package io.composeflow.materialicons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material.icons.automirrored.sharp.ExitToApp
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.sharp.List
import androidx.compose.material.icons.automirrored.sharp.Send
import androidx.compose.material.icons.sharp.AccountBox
import androidx.compose.material.icons.sharp.AccountCircle
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.AddCircle
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.Build
import androidx.compose.material.icons.sharp.Call
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Create
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Done
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Email
import androidx.compose.material.icons.sharp.Face
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material.icons.sharp.KeyboardArrowUp
import androidx.compose.material.icons.sharp.LocationOn
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material.icons.sharp.MailOutline
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Notifications
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material.icons.sharp.Phone
import androidx.compose.material.icons.sharp.Place
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.icons.sharp.ShoppingCart
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material.icons.sharp.ThumbUp
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.serializer.FallbackEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object SharpSerializer : FallbackEnumSerializer<Sharp>(Sharp::class)

/**
 * Enum of Sharp icons from material-icons-core and material-icons-extended libraries
 */
@Serializable(SharpSerializer::class)
@SerialName("Sharp")
enum class Sharp(
    @Transient
    override val imageVector: ImageVector,
    @Transient
    override val packageDescriptor: String = "sharp",
    @Transient
    override val memberDescriptor: String = "Sharp",
) : ImageVectorHolder {
    AccountBox(Icons.Sharp.AccountBox),
    AccountCircle(Icons.Sharp.AccountCircle),
    AddCircle(Icons.Sharp.AddCircle),
    Add(Icons.Sharp.Add),
    ArrowBack(Icons.AutoMirrored.Sharp.ArrowBack),
    ArrowDropDown(Icons.Sharp.ArrowDropDown),
    ArrowForward(Icons.AutoMirrored.Sharp.ArrowForward),
    Build(Icons.Sharp.Build),
    Call(Icons.Sharp.Call),
    CheckCircle(Icons.Sharp.CheckCircle),
    Check(Icons.Sharp.Check),
    Clear(Icons.Sharp.Clear),
    Close(Icons.Sharp.Close),
    Create(Icons.Sharp.Create),
    DateRange(Icons.Sharp.DateRange),
    Delete(Icons.Sharp.Delete),
    Done(Icons.Sharp.Done),
    Edit(Icons.Sharp.Edit),
    Email(Icons.Sharp.Email),
    ExitToApp(Icons.AutoMirrored.Sharp.ExitToApp),
    Face(Icons.Sharp.Face),
    FavoriteBorder(Icons.Sharp.FavoriteBorder),
    Favorite(Icons.Sharp.Favorite),
    Home(Icons.Sharp.Home),
    Info(Icons.Sharp.Info),
    KeyboardArrowDown(Icons.Sharp.KeyboardArrowDown),
    KeyboardArrowLeft(Icons.AutoMirrored.Sharp.KeyboardArrowLeft),
    KeyboardArrowRight(Icons.AutoMirrored.Sharp.KeyboardArrowRight),
    KeyboardArrowUp(Icons.Sharp.KeyboardArrowUp),
    List(Icons.AutoMirrored.Sharp.List),
    LocationOn(Icons.Sharp.LocationOn),
    Lock(Icons.Sharp.Lock),
    MailOutline(Icons.Sharp.MailOutline),
    Menu(Icons.Sharp.Menu),
    MoreVert(Icons.Sharp.MoreVert),
    Notifications(Icons.Sharp.Notifications),
    Person(Icons.Sharp.Person),
    Phone(Icons.Sharp.Phone),
    Place(Icons.Sharp.Place),
    PlayArrow(Icons.Sharp.PlayArrow),
    Refresh(Icons.Sharp.Refresh),
    Search(Icons.Sharp.Search),
    Send(Icons.AutoMirrored.Sharp.Send),
    Settings(Icons.Sharp.Settings),
    Share(Icons.Sharp.Share),
    ShoppingCart(Icons.Sharp.ShoppingCart),
    Star(Icons.Sharp.Star),
    ThumbUp(Icons.Sharp.ThumbUp),
    Warning(Icons.Sharp.Warning),
}
