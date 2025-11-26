# Project: Grin Compression

Authors: Reo Saito

## Resources

*   openjdk version 21.0.8
*   Visual Studio Code: Version: 1.106.2 (Universal)
*   Device: MacBook Pro 14in (2023) with M3 Pro Chip

## Revision Log

*   Wrote most of my implementations without committing halfway through
*   Updated code in the hierarchy class structures in HuffmanTree.java by replacing instanceof leaf for the isLeaf() method by defaulting to false and overriding with true just in the leaf class, since instanceof has to do a pointercheck on the class it belongs to, then if not, then check its superclass, which felt like it will take slightly more resources.
