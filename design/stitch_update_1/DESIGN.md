---
name: Kinetic Discipline
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c4c9ac'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8e9379'
  outline-variant: '#444933'
  surface-tint: '#abd600'
  primary: '#ffffff'
  on-primary: '#283500'
  primary-container: '#c3f400'
  on-primary-container: '#556d00'
  inverse-primary: '#506600'
  secondary: '#bdf4ff'
  on-secondary: '#00363d'
  secondary-container: '#00e3fd'
  on-secondary-container: '#00616d'
  tertiary: '#ffffff'
  on-tertiary: '#621100'
  tertiary-container: '#ffdad2'
  on-tertiary-container: '#bf2b00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c3f400'
  primary-fixed-dim: '#abd600'
  on-primary-fixed: '#161e00'
  on-primary-fixed-variant: '#3c4d00'
  secondary-fixed: '#9cf0ff'
  secondary-fixed-dim: '#00daf3'
  on-secondary-fixed: '#001f24'
  on-secondary-fixed-variant: '#004f58'
  tertiary-fixed: '#ffdad2'
  tertiary-fixed-dim: '#ffb4a2'
  on-tertiary-fixed: '#3c0700'
  on-tertiary-fixed-variant: '#8a1d00'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
typography:
  display-lg:
    fontFamily: Anton
    fontSize: 48px
    fontWeight: '400'
    lineHeight: 52px
    letterSpacing: 0.02em
  headline-lg:
    fontFamily: Anton
    fontSize: 32px
    fontWeight: '400'
    lineHeight: 40px
    letterSpacing: 0.01em
  headline-lg-mobile:
    fontFamily: Anton
    fontSize: 28px
    fontWeight: '400'
    lineHeight: 36px
  title-md:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  container-padding: 16px
  stack-gap: 12px
  section-margin: 32px
  touch-target-min: 48px
---

## Brand & Style

The design system is engineered for high-intensity fitness tracking with a focus on results and discipline. The brand personality is "Athletic Precision"—combining the grit of a local gym with the high-tech accuracy of modern performance metrics. It prioritizes clarity and motivation through high-contrast visuals and an aggressive, mobile-first information hierarchy.

The visual style is a **Modern Material 3** evolution. It utilizes a deep dark theme to reduce visual noise and eye strain during workouts, while neon accents create a "glow" effect that signifies energy and active progress. The aesthetic is clean and purposeful, avoiding decorative elements in favor of functional data visualization.

## Colors

The palette is optimized for OLED displays and high-visibility environments (like outdoor running or bright gyms).

- **Primary (Neon Green):** Represents "Go," success, and active calories burned. Used for primary actions and completion states.
- **Secondary (Electric Blue):** Represents discipline, hydration, and time-based tracking. Used for secondary data points and informational accents.
- **Tertiary (Blaze Orange):** Reserved strictly for warnings, missed targets, or high-intensity zones.
- **Neutral/Surface:** A deep black (#000000) for the base background to maximize contrast, with dark grey (#121212) for elevated card surfaces.

## Typography

Typography balances aggressive motivation with technical readability. 

- **Headlines (Anton):** Chosen for its condensed, powerful impact. It mimics athletic jerseys and traditional sports media. Use all-caps for "Display" and "Headline" roles to instill a sense of urgency and authority.
- **Body (Be Vietnam Pro):** A contemporary sans-serif that remains friendly and highly legible in low-light conditions.
- **Data Labels (Space Grotesk):** A geometric font used for numerical data, timers, and technical labels to give the UI a precise, high-tech instrument feel.

## Layout & Spacing

This design system utilizes a **Mobile-First Fluid Grid** based on an 8px rhythmic scale.

- **Grid:** A 4-column grid for mobile devices with 16px outer margins.
- **Touch Priority:** All interactive elements maintain a minimum 48x48px hit area to accommodate sweaty or moving hands during exercise.
- **Vertical Rhythm:** Content is stacked using a 12px gap for related items and 32px for major logical sections (e.g., separating "Today's Stats" from "Workout Log").
- **Safe Areas:** Strict adherence to Android system bars and navigation cutouts to ensure the UI remains immersive.

## Elevation & Depth

Hierarchy is established through **Tonal Layering** and **Luminescent Accents** rather than traditional shadows.

- **Base Layer:** Pure Black (#000000).
- **Surface Layer:** Dark Grey (#121212) with a subtle 1px inner stroke (10% opacity white) to define edges.
- **Interactive Depth:** Instead of shadows, active elements or "at-risk" metrics use a **Backdrop Glow**. A soft, blurred outer glow using the Primary or Secondary color (15% opacity) suggests that the element is "active" or "energized."
- **Overlays:** Modals and bottom sheets use a 20% blur background (Glassmorphism) to maintain context of the workout screen underneath.

## Shapes

The shape language is **Precision-Rounded**. 

We use `rounded-md` (8px) for standard cards and input fields to maintain a modern, clean look. `rounded-xl` (24px) is reserved for persistent dashboard containers. Buttons and progress indicators utilize a "semi-pill" shape to contrast against the structured grid, signaling interactivity.

## Components

- **Primary Buttons:** High-contrast Neon Green background with Black text. No borders. Heavy use of Anton for the label.
- **Fitness Cards:** Surface-colored cards with a 2px left-accent border (Primary or Secondary color) to categorize data (e.g., Green for Cardio, Blue for Strength).
- **Progress Bars:** Thick 12px tracks. The "filled" portion should have a subtle outer glow effect in the same color as the track to simulate a light-tube.
- **Status Toggles:** Oversized switch components with haptic feedback cues. The "On" state must use the Primary Neon Green.
- **Data Chips:** Small, Space Grotesk-labeled chips with 1px outlines for tagging workout types (e.g., #HIIT, #LegDay).
- **Checkboxes:** Large 24px squares with a heavy 2px border. When checked, they fill completely with Neon Green and provide a "strike-through" animation on the associated text.
- **The "Burn" Indicator:** A specialized component—a circular gauge that pulses slowly when the user is within their target heart rate or calorie-burn zone.