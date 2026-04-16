package com.stafeewa.photocalorie.app.presentation.screens.recipes

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stafeewa.photocalorie.app.R
import com.stafeewa.photocalorie.app.domain.entity.NutritionInfo
import com.stafeewa.photocalorie.app.domain.entity.Recipe
import com.stafeewa.photocalorie.app.presentation.ui.theme.CustomIcons

@Composable
fun RecipeScreen(
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SubscriptionTopBar(
                onRefreshDataClick = {
                    viewModel.processCommand(SubscriptionsCommand.RefreshData)
                },
                onClearRecipesClick = {
                    viewModel.processCommand(SubscriptionsCommand.ClearRecipes)
                }
            )
        },
        content = { contentPadding ->
            val state by viewModel.state.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subscriptions(
                        subscriptions = state.subscriptions,
                        query = state.query,
                        isSubscribeButtonEnabled = state.subscribeButtonEnable,
                        onQueryChanged = {
                            viewModel.processCommand(SubscriptionsCommand.InputTopic(it))
                        },
                        onSubscribeButtonClick = {
                            viewModel.processCommand(SubscriptionsCommand.ClickSubscribe)
                        },
                        onTopicClick = {
                            viewModel.processCommand(SubscriptionsCommand.ToggleTopicSelection(it))
                        },
                        onDeleteSubscription = {
                            viewModel.processCommand(SubscriptionsCommand.RemoveSubscription(it))
                        }
                    )
                }
                if (state.recipes.isNotEmpty()) {
                    item {
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            text = stringResource(R.string.Recipes, state.recipes.size),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        HorizontalDivider()
                    }
                    items(
                        items = state.recipes,
                        key = { it.sourceUrl }
                    ) {
                        RecipeCard(recipe = it)
                    }
                } else if (state.subscriptions.isNotEmpty()) {
                    item {
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.no_recipes_for_selected_subscriptions),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White
                            ),
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionTopBar(
    modifier: Modifier = Modifier,
    onRefreshDataClick: () -> Unit,
    onClearRecipesClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                stringResource(R.string.subscriptions_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.background
                ),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 36.sp
            )
        },
        actions = {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onRefreshDataClick()
                    }
                    .padding(8.dp),
                imageVector = Icons.Default.Refresh,
                tint = Color.White,
                contentDescription = stringResource(R.string.update_recipes)
            )
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onClearRecipesClick()
                    }
                    .padding(8.dp),
                imageVector = Icons.Default.Clear,
                tint = Color.White,
                contentDescription = stringResource(R.string.clear_recipes)
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SubscriptionChip(
    modifier: Modifier = Modifier,
    topic: String,
    isSelected: Boolean,
    onSubscriptionClick: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit

) {
    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = {
            onSubscriptionClick(topic)
        },
        label = {
            Text(topic)
        },
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        onDeleteSubscription(topic)
                    },
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.remove_subscription)
            )
        }
    )
}

@Composable
private fun Subscriptions(
    modifier: Modifier = Modifier,
    subscriptions: Map<String, Boolean>,
    query: String,
    isSubscribeButtonEnabled: Boolean,
    onQueryChanged: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onSubscribeButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(30.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {

            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 5.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.jura))
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (query.isEmpty()) {
                            Text(
                                stringResource(R.string.Search),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.jura))
                            )
                        }
                        innerTextField()
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(60.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.magnifier),
                                contentDescription = "Поиск",
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSubscribeButtonClick,
            enabled = isSubscribeButtonEnabled
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_subscription)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_subscription_button))
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (subscriptions.isNotEmpty()) {
            Text(
                text = stringResource(R.string.subscriptions_label, subscriptions.size),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subscriptions.forEach { (topic, isSelected) ->
                    item(key = topic) {
                        SubscriptionChip(
                            topic = topic,
                            isSelected = isSelected,
                            onSubscriptionClick = onTopicClick,
                            onDeleteSubscription = onDeleteSubscription
                        )
                    }
                }
            }

        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.no_subscriptions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun RecipeCard(
    modifier: Modifier = Modifier,
    recipe: Recipe
) {
    Card(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            .fillMaxWidth(),
        shape = RoundedCornerShape(30.dp)
    ) {
        recipe.image.let { image ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    fontFamily = FontFamily(Font(R.font.jura)),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.image_for_recipe, recipe.title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                NutritionInfoDisplay(nutrition = recipe.nutrition)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = LocalContext.current

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, recipe.sourceUrl.toUri())
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = CustomIcons.OpenInNew,
                            contentDescription = "Read recipe",
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.read))

                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${recipe.title}\n\n${recipe.sourceUrl}"
                                )
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_recipe),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.share))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NutritionInfoDisplay(
    modifier: Modifier = Modifier,
    nutrition: NutritionInfo
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (nutrition.calories.isNotEmpty() ||
            nutrition.protein.isNotEmpty() ||
            nutrition.fat.isNotEmpty() ||
            nutrition.carbs.isNotEmpty()
        ) {
            NutritionRow(label = stringResource(R.string.Calories), value = nutrition.calories)
            NutritionRow(label = stringResource(R.string.Protein), value = nutrition.protein)
            NutritionRow(label = stringResource(R.string.Fat), value = nutrition.fat)
            NutritionRow(label = stringResource(R.string.Carbs), value = nutrition.carbs)
        }
    }
}

@Composable
private fun NutritionRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    if (value.isNotEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily(Font(R.font.jura)),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

