# Cat Sprite Assets

## Current Assets (from opengameart.org/cats-pixel-art)

**License:** CC-BY 4.0 (credit required)
**Source:** https://opengameart.org/content/cats-pixel-art

### Family Cats
Each cat has 5 component parts:
- `Body.png` — main body sprite
- `Head.png` — head (can be rotated/animated separately)
- `Paw.png` — paw (for walking animation)
- `Pupil_big.png` — large pupil (surprised/alert)
- `Pupil_small.png` — small pupil (calm/sleepy)

| Family Member | Cat Colour | Folder |
|---------------|------------|--------|
| Will (Dad) | Orange | `will_cat/` |
| Lucy (Mum) | White | `lucy_cat/` |
| Imogen (Daughter) | Grey | `imogen_cat/` |

### Accessories
- `Bell.png` — collar bell
- `Catnip.png` — catnip toy
- `Fish.png` — fish toy
- `Mouse.png` — mouse toy
- `Yarn.png` — yarn ball

## Animation States Needed

Based on reference from [salt-desktop-pet](https://github.com/magpieee/salt-desktop-pet):

| State | Description | Frames Needed |
|-------|-------------|---------------|
| IDLE | Sitting, looking around | 4-6 frames |
| SLEEPING | Eyes closed, breathing | 4 frames |
| WAKING | Stretching, yawning | 6-8 frames |
| WALKING | Moving left/right | 6 frames |
| WORKING | Typing/reading | 4-6 frames |
| PLAYING | Running, jumping | 8 frames |
| EATING | Munching | 4 frames |
| EXCITED | Jumping, sparkles | 8 frames |
| SAD | Ears down, rain | 4 frames |
| TALKING | Mouth open/close | 2-4 frames |
| THINKING | Thought bubble | 4 frames |
| WAVING | Wave animation | 6 frames |

## Animation Atlas Format

Each cat should have a **sprite sheet** (atlas) with:
- Rows = animation states
- Columns = frames per animation
- Standard size: 32x32 or 64x64 per frame

Example layout:
```
| idle_0 | idle_1 | idle_2 | idle_3 |
| sleep_0 | sleep_1 | sleep_2 | sleep_3 |
| walk_0 | walk_1 | walk_2 | walk_3 |
| ... |
```

## Next Steps

1. **Create animation frames** from the base parts (head rotation, paw movement)
2. **Generate sprite sheets** for each cat colour
3. **Add accessories** as overlay sprites
4. **Test in Jetpack Compose Canvas**

## References

- `reference-salt-animation-atlas.png` — Full animation atlas from salt-desktop-pet
- `*.gif` files — Individual animation previews (idle, running, waving, etc.)
