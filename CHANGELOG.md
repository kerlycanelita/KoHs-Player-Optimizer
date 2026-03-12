# Changelog

All notable changes to **KoHs Player Optimizer** are documented in this file.

This project follows a beta-first release flow. Until a stable `1.0.0` release, settings, internals, behavior, and compatibility may change between versions.

## [0.1.0-beta.1] - 2026-03-12

> [!WARNING]
> **BETA WARNING (IMPORTANT):** This mod is still in beta and currently contains known bugs and edge cases.
> Use in production modpacks at your own risk. More fixes are planned and will be shipped incrementally.

### Full Rewrite Scope
- Reworked all core optimization feature modules to remove no-op and partial logic paths.
- Replaced the previous config screen pipeline with a custom futuristic dark-purple UI stack.
- Expanded runtime debug instrumentation and added in-game live diagnostics controls.

### Added
- New configurable presets system:
  - `Crystal PvP`
  - `Netherite PvP`
  - `Mace PvP`
  - `Sword PvP`
  - `UHC`
  - `Custom`
- Preset snapshot persistence:
  - Selecting any preset applies a full profile.
  - Returning to `Custom` restores the user’s previous custom values.
- New custom GUI components:
  - `KohsButtonWidget`
  - `KohsToggleWidget`
  - `KohsSliderWidget`
  - `KohsScrollPanel`
  - `KohsGuiColors`
  - `KohsGuiUtils`
- New HUD editor screen for debug panel positioning:
  - `DebugPanelHudEditorScreen`
- Realtime debug panel:
  - In-game live tree panel with per-section visibility toggles.
  - Warning modal before enabling the panel, with "Don’t show again" support.

### Changed
- `Block Interior Rendering`:
  - Multi-point in-block sampling.
  - Smoothed alpha transitions with easing.
  - Overlay alpha controls wired to actual game overlays.
  - Optional water/lava suppression now applied by mixins.
- `Player Movement`:
  - EMA-based smoothing.
  - Distance-adaptive smoothing behavior.
  - Micro-jitter dead-zone filtering.
  - Velocity-based extrapolation metrics.
- `Block Placement`:
  - Debounce and prediction map now applied on real block-interact path.
  - Capacity limits and adaptive timeout retained and fully connected.
  - Placement confirmation and timeout telemetry exposed.
- `Combat Visuals`:
  - Combo tracker now increments on real successful hit callbacks.
  - Cooldown visuals and pulse pipeline refined.
- `Interaction Optimization`:
  - Buffered input replay now hooks into real client attack/use methods.
  - Added stale-buffer handling and metrics.
  - Added smart double-click detection options.
- `Config & Wiring`:
  - Expanded `OptimizerConfig` with new sections and debug panel fields.
  - Added config normalization and custom preset snapshot initialization.
  - Updated client/feature manager bridge methods for mixins.

### Fixed
- Multiple feature paths that previously had no gameplay effect are now actively wired.
- Combo tracking no longer depends only on click edge and now tracks entity hit events.
- Block placement RTT/timeout update path corrected.
- Compatibility condition checks corrected in movement optimization logic.
- Build and mixin wiring issues resolved for the current `1.21.10` target.

### Mixin Integration
- Added and wired:
  - `ClientPlayerInteractionManagerMixin`
  - `MinecraftClientInvoker`
- Reworked:
  - `InGameOverlayRendererMixin`
- Updated mixin config to include new runtime hooks used by rewritten features.

### Verification
- Automated build checks completed successfully:
  - `./gradlew :versions:mc1_21_10:compileClientJava`
  - `./gradlew :versions:mc1_21_10:build`

### Known Issues / Beta Limitations
- Compatibility with larger public modpacks is still under active validation.
- Some visual overlays and debug outputs may conflict with external HUD/overlay mods.
- Preset balancing may be adjusted after more PvP field testing.
- Realtime debug panel can reduce FPS on lower-end systems when many nodes are visible.
- Additional bug fixes and behavior tuning are planned for upcoming beta patches.

### Upgrade Notes
- Existing config files may be normalized or migrated at runtime.
- Presets now control multiple subsystems together; review all values after switching presets.
- If behavior looks inconsistent after update, regenerate config and re-apply your desired preset.

## Upcoming (Planned)
- Continued beta stabilization and bugfix-focused patches.
- Expanded compatibility layer coverage and stronger conflict diagnostics.
- Additional GUI polish and performance tuning for debug tooling.
