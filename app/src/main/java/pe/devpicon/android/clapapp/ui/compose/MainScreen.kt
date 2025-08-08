package pe.devpicon.android.clapapp.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pe.devpicon.android.clapapp.R // Import for your project's resources

@Composable
fun MainScreen(onLaunchClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onLaunchClick,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_circle_double),
                contentDescription = stringResource(id = R.string.tap_here),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(
                    colorResource(id = R.color.button_main)
                )
            )
        }
        Text(
            text = stringResource(id = R.string.tap_here),
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultMainScreenPreview() { // Renamed preview to avoid conflict if MainActivity also has one
    // Assuming you might have a theme like ClapAppDemoTheme in this package or a parent one.
    // If your theme is in pe.devpicon.android.clapapp.ui.theme.YourThemeName:
    // import pe.devpicon.android.clapapp.ui.theme.ClapAppDemoTheme
    // ClapAppDemoTheme {
    MainScreen(onLaunchClick = {})
    // }
}
