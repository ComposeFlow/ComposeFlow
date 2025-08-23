package io.composeflow.model.project.appscreen.screen.composenode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.TabContentTrait
import io.composeflow.model.parameter.TabTrait
import io.composeflow.model.parameter.TabsTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.swap
import kotlinx.serialization.Transient

/**
 * Interface that defines specific functionalities for TabNode
 */
interface TabsNode {
    var self: ComposeNode
    val selectedIndex: MutableState<Int>

    fun addTab()

    fun removeTab(index: Int)

    fun isVisibleInCanvas(): Boolean

    fun swapTabIndex(
        from: Int,
        to: Int,
    )

    /**
     * Updated to selectedIndex of the Tabs node to the index where this node belongs to.
     *
     * This is used to make monitoring the selected TabContainer in the canvas easier.
     * Typically, a TabContainer consists of following set of ComposeNodes, and we want to switch
     * the selected tab when either of following situation happens:
     *   when a Tab is focused
     *   when a TabContent (or any of its children) is focused
     *
     * - Tabs (TabContainer)
     *   (selectedIndex is set to 1 ( the index where the Text belongs to))
     *   - TabRow
     *     - Tab
     *     - Tab
     *     - Tab
     *   - TabContent
     *     - Text
     *     - ...
     *   - TabContent
     *     - Text ( setTabSelectedIndex() is called on this node)
     *     - ...
     *   - TabContent
     *     - Text
     *     - ...
     */
    fun setTabSelectedIndex()
}

class TabsNodeImpl : TabsNode {
    override lateinit var self: ComposeNode

    /**
     * Only used when the trait is [TabsTrait]
     */
    @Transient
    override val selectedIndex: MutableState<Int> = mutableStateOf(0)

    override fun addTab() {
        check(self.trait.value is TabsTrait)
        val tabRow = self.children.first()
        val nextIndex = tabRow.children.size + 1
        tabRow.addChild(
            ComposeNode(
                trait =
                    mutableStateOf(
                        TabTrait(
                            text = StringProperty.StringIntrinsicValue("Tab $nextIndex"),
                            index = nextIndex - 1,
                        ),
                    ),
            ),
        )
        self.addChild(
            ComposeNode(
                trait = mutableStateOf(TabContentTrait),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.FillMaxSize(),
                    ),
            ).apply {
                children.add(
                    ComposeNode(
                        modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.Padding(8.dp)),
                        trait =
                            mutableStateOf(
                                TextTrait(
                                    text = StringProperty.StringIntrinsicValue("Tab content $nextIndex"),
                                    colorWrapper =
                                        ColorProperty.ColorIntrinsicValue(
                                            ColorWrapper(
                                                themeColor = Material3ColorWrapper.OnBackground,
                                            ),
                                        ),
                                ),
                            ),
                    ),
                )
            },
        )
    }

    override fun removeTab(index: Int) {
        check(self.trait.value is TabsTrait)
        val tabRow = self.children.first()
        tabRow.children.removeAt(index)
        tabRow.children.forEachIndexed { i, tab ->
            tab.trait.value = (tab.trait.value as TabTrait).copy(index = i)
        }
        // The first child is assigned as TabRow. TabContent starts with the second index
        self.children.removeAt(index + 1)
    }

    override fun setTabSelectedIndex() {
        getNearestTabIndex()?.let { index ->
            val tabs =
                self
                    .findNodesUntilRoot(includeSelf = true)
                    .firstOrNull { it.trait.value is TabsTrait }
            tabs?.selectedIndex?.value = index
        }
    }

    override fun isVisibleInCanvas(): Boolean =
        !isPartOfTabContent() ||
            getNearestTabIndex() == getNearestSelectedTabIndex()

    override fun swapTabIndex(
        from: Int,
        to: Int,
    ) {
        check(self.trait.value is TabsTrait)
        val tabs = self.children.first().children
        tabs.swap(from, to)

        // Swap the TabContent shifted by 1 because the first child is used for TabRow
        self.children.swap(from + 1, to + 1)
    }

    /**
     * Returns the index of the nearest tab where this node belongs to, or null if it's not part of
     * a Tabs
     */
    private fun getNearestTabIndex(): Int? {
        val tabs =
            self
                .findNodesUntilRoot(includeSelf = true)
                .firstOrNull { it.trait.value is TabsTrait }
        val tabContent =
            self
                .findNodesUntilRoot(includeSelf = true)
                .firstOrNull { it.trait.value == TabContentTrait }
        val tabContents = tabs?.children?.drop(1)
        var result: Int? = null
        tabContents?.forEachIndexed { i, content ->
            if (tabContent == content) {
                result = i
            }
        }
        return result
    }

    private fun getNearestSelectedTabIndex(): Int? =
        self
            .findNodesUntilRoot(includeSelf = true)
            .firstOrNull { it.trait.value == TabsTrait }
            ?.selectedIndex
            ?.value

    private fun isPartOfTabContent(): Boolean =
        self
            .findNodesUntilRoot(includeSelf = true)
            .any { TraitCategory.TabContent in it.trait.value.paletteCategories() }
}
