package com.garrettbutchko.minimate.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.garrettbutchko.minimate.content.helper.LogoLoadingView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.garrettbutchko.minimate.dataModels.courseModels.Course
import com.garrettbutchko.minimate.shared.R.color

/**
 * CourseButtonView
 *
 * A tappable card row representing a single course in the course list.
 * Mirrors iOS CourseButtonView — shows course name, ID, and either a logo image
 * (async loaded with Coil) or a trailing chevron.
 *
 * iOS AsyncImage states → Coil SubcomposeAsyncImage loading/success/error.
 * iOS .cardShadow() → Surface tonalElevation.
 * iOS .sub color → MaterialTheme.colorScheme.surfaceVariant
 * iOS .mainOpp → MaterialTheme.colorScheme.onSurface
 * iOS .subOpp → MaterialTheme.colorScheme.onSurfaceVariant
 */
@Composable
fun CourseButtonView(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .subtleShadow(),
        shape = RoundedCornerShape(25.dp),
        color = colorResource(id = color.sub),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colorResource(id = color.mainOpp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = color.subOpp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (course.logo != null) {
                // Async logo image via Coil SubcomposeAsyncImage
                SubcomposeAsyncImage(
                    model = course.logo,
                    contentDescription = "${course.name} logo",
                    modifier = Modifier.height(50.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        LogoLoadingView(size = 24.dp)
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "Logo unavailable",
                            tint = colorResource(id = color.subOpp)
                        )
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select course",
                    tint = colorResource(id = color.mainOpp)
                )
            }
        }
    }
}
