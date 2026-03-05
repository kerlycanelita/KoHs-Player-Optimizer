# Compatibility Analysis (Sodium, Lithium, Reese's Sodium Options)

This module is intentionally designed to avoid deep renderer and engine overrides that commonly conflict with major optimization mods.

## Sodium

- Sodium replaces large parts of the client renderer and chunk pipeline.
- This mod only applies a narrow in-wall overlay suppression hook and does not patch chunk meshing or render graph internals.
- A dedicated compatibility setting can disable rendering hooks while Sodium is loaded.

## Lithium

- Lithium focuses on server-side and simulation-level optimizations, with limited direct overlap on client rendering logic.
- This mod keeps movement smoothing purely visual and client-local.
- Movement hooks can be disabled if users observe edge-case behavior with mixed mod stacks.

## Reese's Sodium Options

- Reese's Sodium Options modifies options UI flow and Sodium settings UX.
- This mod's config interface uses Mod Menu + Cloth Config and does not modify Sodium options screens directly.
- Optional UI hooks can be disabled to keep the configuration experience isolated.

## Conflict-Avoidance Strategy

- Keep all combat, interaction, and placement optimizations client-side.
- Avoid packet spoofing, forced server state changes, or logic that can be interpreted as cheating.
- Use small, explicit hooks with kill switches through compatibility settings.
- Keep feature boundaries modular so future versions can replace internals without touching unrelated systems.
