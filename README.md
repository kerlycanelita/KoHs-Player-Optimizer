# KoHs Player Optimizer

High-performance Fabric optimization mod workspace for Minecraft versions **1.21** through **1.21.10**.

## Project Status

This project is currently in **active development** and tagged as **beta**.

- Release phase: `pre-1.0.0` (beta)
- Stability profile: feature-complete baseline for `1.21.10`, but still under iterative tuning
- Compatibility profile: designed for legitimate multiplayer-safe client behavior, with ongoing validation against wider mod stacks
- API/config contract: settings and internal module structure may change between beta revisions

Until the first stable release, expect:

- potential breaking configuration changes
- optimization strategy refinements
- compatibility adjustments based on real-world server/modpack feedback

## Version Layout

- Active implementation target: `versions/1.21.10` (fully buildable)
- Scaffold-only targets: `versions/1.21` through `versions/1.21.9`

## Build

```bash
./gradlew :versions:mc1_21_10:build
```

## GitHub Publication Note

The repository is being prepared for GitHub publication while the mod remains in beta.
Documentation, release notes, and compatibility matrices will be expanded as the project approaches a stable tag.
