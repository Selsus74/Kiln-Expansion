# Kiln Expansion

**Kiln Expansion** adds Multiblock-Kilns which can be used for firing clay or smelting iron. Cooler Furnace. More drip.

Charcoal Pile supports vanilla Logs and Woods incl. their stripped counterparts.
This is done through a custom tag for each: **charable_logs** and **charable_woods**.
All Registries are filtered by Id, checking if they have **_log** or **_wood** as a suffix.
Modded Logs and Woods are not supported out of the box. The TagDataGen can only add what is currently loaded Registries,
making it impossible to add modded Logs or Woods automatically.
To add support for all loaded mods, the Tags need to be altered after all mods are initialized.
This is rather easy tho and the simplest Option for this would be a KubeJS Serverside Script with the TagEvent.
These Scripts are triggered on joining the World or running a **/reload** Command.
Making it passible to iterate over all registered Blocks and with the Help of RegEx or other String-Parsing-Methods,
add the Tags to the Blocks with corresponding Suffixes.

### Example KubeJS Script:
```javascript
ServerEvents.tags('block', event => {
//---all wood and logs are under the minecraft:logs tag---
  event.get('minecraft:logs').getObjectIds().forEach(id => {
    if (id.path.endsWith('_wood'))
      event.add('kilnexpansion:charable_woods', id)
    else
      event.add('kilnexpansion:charable_logs', id)
  })
})
```