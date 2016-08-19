package ink.abb.pogo.api.cache

import POGOProtos.Data.Player.PlayerStatsOuterClass.PlayerStats
import POGOProtos.Data.PokedexEntryOuterClass
import POGOProtos.Enums.PokemonFamilyIdOuterClass.PokemonFamilyId
import POGOProtos.Enums.PokemonIdOuterClass
import POGOProtos.Inventory.AppliedItemOuterClass
import POGOProtos.Inventory.EggIncubatorOuterClass.EggIncubator
import POGOProtos.Inventory.InventoryDeltaOuterClass
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class Inventory {
    var currencies = mutableMapOf<String, AtomicInteger>()
    var items = mutableMapOf<ItemId, AtomicInteger>()
    var pokemon = mutableMapOf<Long, BagPokemon>()
    var appliedItems = mutableListOf<AppliedItemOuterClass.AppliedItem>()
    var candies = mutableMapOf<PokemonFamilyId, AtomicInteger>()

    var eggIncubators = mutableListOf<EggIncubator>()

    var size = AtomicInteger(350)

    var gems = AtomicInteger(0)

    lateinit var playerStats: PlayerStats

    var pokedex = mutableMapOf<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry>()

    fun update(inventoryDelta: InventoryDeltaOuterClass.InventoryDelta) {
        val items: MutableMap<ItemId, AtomicInteger>
        val pokemon: MutableMap<Long, BagPokemon>
        val candies: MutableMap<PokemonFamilyId, AtomicInteger>

        val size: AtomicInteger

        val gems: AtomicInteger

        val pokedex: MutableMap<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry>

        if (inventoryDelta.originalTimestampMs == 0L) {
            items = mutableMapOf<ItemId, AtomicInteger>()
            pokemon = mutableMapOf<Long, BagPokemon>()
            candies = mutableMapOf<PokemonFamilyId, AtomicInteger>()

            size = AtomicInteger(350)

            gems = AtomicInteger(0)

            pokedex = mutableMapOf<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry>()
        } else {
            items = this.items
            pokemon = this.pokemon
            candies = this.candies
            size = this.size
            gems = this.gems
            pokedex = this.pokedex
        }
        inventoryDelta.inventoryItemsList.forEach {
            val itemData = it.inventoryItemData
            if (itemData.hasAppliedItems()) {
                this.appliedItems = itemData.appliedItems.itemList
            }
            if (itemData.hasCandy()) {
                candies.getOrPut(itemData.candy.familyId, { AtomicInteger(0) }).set(itemData.candy.candy)
            }
            if (itemData.hasEggIncubators()) {
                this.eggIncubators = itemData.eggIncubators.eggIncubatorList
            }
            if (itemData.hasInventoryUpgrades()) {
                var total = 350
                itemData.inventoryUpgrades.inventoryUpgradesList.forEach {
                    total += it.additionalStorage
                }
                size.set(total)
            }
            if (itemData.hasItem()) {
                items.getOrPut(itemData.item.itemId, { AtomicInteger(0) }).set(itemData.item.count)
            }
            if (itemData.hasPlayerCurrency()) {
                // ???
                gems.set(itemData.playerCurrency.gems)
            }
            if (itemData.hasPlayerStats()) {
                playerStats = itemData.playerStats
            }
            if (itemData.hasPokedexEntry()) {
                pokedex.set(itemData.pokedexEntry.pokemonId, itemData.pokedexEntry)
            }
            if (itemData.hasPokemonData()) {
                pokemon.put(itemData.pokemonData.id, BagPokemon(itemData.pokemonData))
            }
        }
        if (inventoryDelta.originalTimestampMs == 0L) {
            this.items = items
            this.pokemon = pokemon
            this.candies = candies
            this.pokedex = pokedex
        }
    }
}