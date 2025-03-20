# Unnamed Anticheat
A 1.8.9 anticheat I have worked on some time ago for quite a while, never finished it. Posting this to show how a simulation anticheat can work. I'm slowly working on a better multi version anticheat with things I learned while writing this anticheat.

This anticheat is not meant to be compiled or used its only here for **educational purposes about simulation engines**.

## Good stuff
Theres some nice concepts in the anticheat like SplitState/ConfirmableState which allows a specific state to be in 2 states at once to account for transaction splitting. (nearly every block is fully lag compensated on both state level and bounding box level by checking neighbours)
There's also a mostly "working" area based simulation engine which has quite support for mostly everything in 1.8.9

## Bad stuff
Some parts are written quite poorly and need a complete rewrite (eg InventoryTracker), some parts are purely just a proof of concept. Another limitation is that the start area is only one area (optimally this should support multiple areas to have less leniency). Theres also a half baked cloud based system I was working on, the cloud server is not provided in source. Theres probs more bad stuff but you'll see it in the code!


## Credits
The reach and timer part is mostly based on [Dusk](https://github.com/ThomasOM/Dusk), I would recommend taking a look.


## Questions
If you have any questions feel free to contact me on discord @1.7.10 (or join the [Minecraft Anticheat Community](https://discord.gg/gMnCjxGq3e)).
I also have telegram [@Beaness](http://t.me/Beaness).