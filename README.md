# SharedInventory
Dupe-free, vanish-free, no-compromise inventory sharing mod for Minecraft 1.21.1

Inventory sharing is based upon teams, so you can choose how the inventories of your players are shared.

This mod is server-side only and does not require any client-side installation\*. Needs Fabric API.

*\*: you may need a bug-fix mod on the client side if you expect to use the creative inventory. See CreativeInventoryFix.*

# Edge-cases
A player joining a team will have their current inventory thrown on the ground and their inventory synchronized, unless that player is the first to join that team.

A player leaving a team will see their inventory cleared, unless that player is the last to leave that team.

A player dying will drop their inventory on the ground as usual, that means that other players from the same team will see their inventories cleared.

# How to use

    /share <true/false>

# Issues
If you find any issues (vanishing items, duped items, etc.) please report them on the issue tracker in github, the goal of this mod is to be as stable as possible.
