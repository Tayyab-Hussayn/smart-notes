# UI Design System

**Project:** Smart Sticky Wallpaper  
**Phase:** 04 — UX/UI Specification  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the visual language, semantic design tokens, reusable components, interaction states, accessibility foundations, responsive behavior, and visual QA rules.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `UX_SPECIFICATION.md`

---

## 1. Design Direction

The interface should feel:

- calm,
- personal,
- lightweight,
- tactile but not cartoonish,
- modern,
- readable,
- functional before decorative.

The wallpaper/background contributes personality; notes and readability remain primary.

---

## 2. Design Principles

1. Semantic tokens over arbitrary values.
2. Content over chrome.
3. Sticky notes remain the primary visual object.
4. Visual hierarchy must remain legible over varied wallpapers.
5. Native conventions may override visual uniformity when accessibility/usability improves.
6. Meaning must not depend on color alone.
7. Motion communicates state rather than decoration.
8. AI and monetization must never visually impersonate user content.
9. Dense canvases require visual de-emphasis, not uncontrolled clutter.
10. Dark mode is designed intentionally.

---

## 3. Token Architecture

Use semantic tokens grouped into:

- color,
- typography,
- spacing,
- shape,
- elevation,
- opacity,
- motion,
- focus,
- component state.

Components consume semantic roles rather than hard-coded platform-specific styling.

---

## 4. Color Roles

Core semantic roles:

- background,
- background-overlay,
- surface,
- elevated-surface,
- text-primary,
- text-secondary,
- text-disabled,
- border,
- focus-ring,
- accent,
- success,
- warning,
- error,
- information,
- scrim.

Sticky-note palette roles should be separate from generic system status colors.

---

## 5. Sticky Note Color System

Note palettes must preserve readable text contrast in:

- light theme,
- dark theme,
- busy wallpaper,
- high-contrast/accessibility conditions.

A note color must not alone communicate:

- priority,
- reminder state,
- AI origin,
- error state.

Use icons/text/state indicators too.

---

## 6. Typography

Semantic levels:

- Display,
- Heading,
- Title,
- Body,
- Label,
- Caption.

Use system/platform fonts unless branding requirements justify otherwise.

Typography must support:

- large text scaling,
- long note content,
- localization expansion,
- mixed-script text,
- readable line height.

---

## 7. Spacing

Baseline spacing scale:

`4 / 8 / 12 / 16 / 24 / 32 / 48`

Use tokens rather than arbitrary margins.

Compact variants may reduce spacing without reducing touch-target accessibility.

---

## 8. Shape

Suggested semantic shape roles:

- small control,
- medium surface,
- sticky note,
- modal/dialog,
- pill/chip.

Notes should look distinct enough to feel spatial without excessive skeuomorphism.

---

## 9. Elevation / Depth

Depth should distinguish:

- wallpaper/background,
- notes,
- selected note,
- transient menus/dialogs.

Avoid excessive shadow stacks and glass effects.

Selected/dragging states may increase elevation subtly.

---

## 10. Sticky Note Component Anatomy

Possible anatomy:

```text
┌─────────────────────┐
│ optional status row │
│ Title               │
│ Body                │
│                     │
│ reminder / pin      │
└─────────────────────┘
```

Content remains dominant.

Persistent action buttons should be minimal; secondary actions appear on selection/context menu/editor.

---

## 11. Sticky Note States

Required design states as applicable:

- default,
- hover,
- focused,
- pressed,
- selected,
- dragging,
- resizing,
- editing,
- pinned,
- contextually highlighted,
- snoozed/de-emphasized,
- disabled/unavailable action.

Contextual surfacing must not visually look like destructive replacement of the user's layout.

---

## 12. Authored vs AI Visual Language

User-authored content is visually primary.

AI-derived suggestions may use:

- small sparkle/intelligence icon,
- concise “Suggested” label,
- reason text,
- separate suggestion surface.

Do not rewrite authored text visually as though the user wrote AI-generated content.

---

## 13. Canvas Layers

Deterministic layer order:

1. wallpaper/background,
2. optional readability treatment,
3. standard notes,
4. pinned/priority notes,
5. contextual highlight treatment,
6. selection/drag handles,
7. transient menus/popovers,
8. app/system controls.

Ads do not enter the note layer.

---

## 14. Background Readability

Because wallpaper content is unpredictable, the design system should support controlled readability aids such as:

- note fill opacity,
- border,
- subtle shadow,
- local scrim/backplate,
- user-adjustable background treatment where justified.

Do not rely on blur/glass alone for legibility.

---

## 15. Core Components

Reusable component families:

### Actions
- primary button,
- secondary button,
- destructive button,
- icon button,
- menu item.

### Inputs
- text field,
- multiline editor,
- search,
- switch,
- slider,
- chip,
- date/time controls,
- recurrence controls.

### Containers
- dialog,
- bottom sheet,
- popover,
- side panel,
- card/list row.

### Feedback
- snackbar,
- banner,
- inline validation,
- progress state,
- tooltip.

---

## 16. Note Editor Component

Visual hierarchy:

1. content/title,
2. reminder summary,
3. pin/visibility,
4. appearance,
5. intelligence/context,
6. lifecycle actions.

Advanced settings remain collapsed until requested.

---

## 17. Surface Controls

Core surface controls:

- Add,
- Search,
- Filter,
- View/arrange where supported,
- Surface settings.

On compact screens, secondary controls may enter an overflow/menu.

---

## 18. Reminder Components

Define components for:

- reminder summary chip,
- due state,
- recurring indicator,
- snooze action,
- complete action,
- privacy-aware notification preview settings.

Reminder state should be understandable without color alone.

---

## 19. Sync Status Components

Use low-noise status patterns:

- synced,
- pending,
- offline,
- retrying,
- needs attention.

Do not permanently occupy prominent space when sync is healthy.

---

## 20. AI Components

Possible components:

- suggestion card,
- reason chip,
- processing indicator,
- confirm/edit/reject controls,
- AI unavailable state.

AI failure styling should not imply note-data failure.

---

## 21. Monetization Components

Separate from core note components:

- feature comparison,
- subscription status,
- purchase CTA,
- restore action,
- manage subscription,
- entitlement badge where useful,
- ad container.

Never use fake disabled note content to manufacture paywall pressure.

---

## 22. Ad Container Rules

Ad containers must:

- be visually distinct from notes,
- include required advertising labeling,
- preserve spacing from interactive note controls,
- disappear cleanly on no-fill/failure,
- not overlap keyboard/editor,
- not imitate OS alerts.

---

## 23. Focus System

Keyboard-capable platforms require visible focus treatment.

Focus token must work on:

- light/dark themes,
- colored notes,
- busy wallpapers.

Selection and keyboard focus should be distinguishable where both can occur.

---

## 24. Touch Targets

Interactive targets must meet platform accessibility guidance.

Small visual icons may have larger invisible hit areas.

Resize handles must not become impossible to use on touch devices.

---

## 25. Motion

Use motion for:

- note creation,
- selection,
- drag/drop,
- panel transitions,
- surfacing/de-emphasis,
- completion.

Motion should be brief, deterministic, interruptible, and reduced/removed when reduced-motion settings require it.

---

## 26. Responsive Layout

### Phone
- canvas fills most viewport,
- compact navigation,
- editor may use full screen/bottom sheet.

### Tablet
- optional dual-pane editor/collection,
- larger spatial canvas.

### Desktop
- sidebar/rail possible,
- context menus/tooltips,
- resizable windows/panels,
- keyboard shortcuts.

Canvas controls must not unnecessarily cover notes.

---

## 27. Theme System

Support where practical:

- system theme,
- light,
- dark.

Theme changes should not modify authored note semantics.

Dark mode must adjust note palette/contrast intentionally.

---

## 28. System Integration

Use native patterns for:

- system bars,
- dialogs,
- context menus,
- permissions,
- notifications,
- keyboard behavior,
- window management,
- billing surfaces.

Shared UI must not expose native SDK types in product-level component APIs.

---

## 29. Empty / Error / Offline Components

Design consistent states for:

- empty collection,
- no search results,
- offline,
- sync pending,
- permission denied,
- AI unavailable,
- provider failure,
- destructive confirmation.

State styling should communicate impact accurately.

---

## 30. Accessibility Tokens & Rules

Design system must support:

- contrast-safe text/background pairs,
- visible focus,
- text scaling,
- non-color-only indicators,
- semantic component labels,
- reduced motion,
- sufficient hit targets,
- accessible error text.

Accessibility cannot be fixed only at screen level; components must encode it.

---

## 31. Localization Readiness

Components must tolerate:

- longer labels,
- text expansion,
- right-to-left layout if localization later requires it,
- mixed-language note content,
- different date/time formatting.

Avoid fixed-width assumptions for text-heavy controls.

---

## 32. Visual QA Matrix

Validate at minimum:

- light + dark themes,
- busy/light/dark wallpaper,
- large text,
- high-contrast conditions,
- keyboard focus,
- touch/mouse,
- long note content,
- many notes,
- offline/error states,
- AI suggestion/failure,
- premium/free/ad states,
- localization expansion.

---

## 33. Design Token Freeze Policy

Exact brand colors, type sizes, corner radii, animation durations, and component measurements may remain token-level implementation decisions until visual prototyping validates them.

However, semantic roles and accessibility constraints must be stable before final freeze.

---

## 34. Consolidated Review Criteria

Verify:

- all Phase 4 UX states have design-system support,
- AI is visually distinguishable from authored state,
- ads cannot masquerade as notes,
- responsive behavior fits all target platform families,
- accessibility tokens/states are testable,
- no component requires unsupported cross-platform parity.

Phase 4 remains **Draft** until final project freeze.
