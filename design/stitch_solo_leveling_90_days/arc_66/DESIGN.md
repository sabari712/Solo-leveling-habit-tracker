---
name: Arc 66
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#3a3939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c4c6cc'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8e9196'
  outline-variant: '#44474c'
  surface-tint: '#bac8dc'
  primary: '#bac8dc'
  on-primary: '#243141'
  primary-container: '#0d1b2a'
  on-primary-container: '#768497'
  inverse-primary: '#525f71'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#f9bd22'
  on-tertiary: '#402d00'
  tertiary-container: '#241800'
  on-tertiary-container: '#a67c00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d6e4f9'
  primary-fixed-dim: '#bac8dc'
  on-primary-fixed: '#0f1c2c'
  on-primary-fixed-variant: '#3a4859'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdf9f'
  tertiary-fixed-dim: '#f9bd22'
  on-tertiary-fixed: '#261a00'
  on-tertiary-fixed-variant: '#5c4300'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
  discipline-navy: '#0D1B2A'
  growth-emerald: '#10B981'
  level-up-gold: '#FBBF24'
  obsidian-base: '#0D0D0D'
  steel-gray: '#797B85'
  action-orange: '#FB6900'
typography:
  display-hero:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  title-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.1em
  quest-stat:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 8px
  gutter: 16px
  margin-mobile: 20px
  margin-desktop: 40px
  container-max: 1200px
---

## Brand & Style
The design system is built on the concept of the "Hero’s Journey" through a 66-day habit cycle. It targets high-achievers and self-improvement enthusiasts who respond to gamification and structured progress. The brand personality is disciplined, rewarding, and immersive—transforming mundane tasks into "Daily Quests."

The design style is **Modern Game Interface**. It combines the sleekness of high-end SaaS with the tactical, rewarding feedback loops of RPG interfaces. This is achieved through dark mode by default, high-contrast interactive elements, and a "Tonal Layering" approach that mimics a physical control console. The interface should feel focused and intentional, minimizing distractions to facilitate deep work and habit formation.

## Colors
The palette is centered on "Discipline Navy" for the primary structural elements, providing a deep, focused environment. "Growth Emerald" is utilized for success states, completed quests, and positive progress bars. "Level-up Gold" is the high-visibility accent reserved for achievements, streak milestones, and rarity indicators.

The default mode is **Dark**. The background uses "Obsidian Base" (#0D0D0D) to ensure the chromatic colors pop with high contrast. "Action Orange" from the reference material is repurposed as a critical-path color for high-priority urgent quests or "broken streak" warnings.

## Typography
The typographic hierarchy emphasizes clarity and technical precision. **Hanken Grotesk** provides a strong, modern sans-serif voice for headings that feels athletic and disciplined. **Inter** handles all body copy to ensure maximum readability during long-form reflections or habit descriptions. 

To reinforce the RPG/Game aesthetic, **JetBrains Mono** is used for labels, metadata (like XP values), and timers. This monospaced font adds a "system readout" feel to the progress metrics. For mobile, headline sizes scale down aggressively to ensure stats remain visible "above the fold" without excessive scrolling.

## Layout & Spacing
This design system utilizes a **8px linear scale** for all spacing and sizing. The layout follows a **Fluid Grid** model with a 12-column structure for desktop and a 4-column structure for mobile.

The spacing rhythm is tight to create a "cockpit" feel where information is densely but clearly packed. Large margins (20px+) are used on the outer edges of the mobile viewport to provide "thumb-room" and prevent accidental touches on edge-aligned elements. Sections are separated by distinct tonal shifts rather than just whitespace to maintain the immersive game UI aesthetic.

## Elevation & Depth
Depth is conveyed through **Tonal Layers** and **Low-Contrast Outlines**. Instead of traditional shadows, which can feel too "web-standard," this system uses surface lighting.

- **Level 0 (Floor):** Obsidian Base (#0D0D0D).
- **Level 1 (Cards):** Discipline Navy (#0D1B2A) with a 1px subtle border (#797B85 at 20% opacity).
- **Level 2 (Active Elements):** Primary Navy with a "Growth Emerald" glow effect (inner shadow) to indicate an active quest.
- **Interactive Depth:** When a user interacts with a card, it shouldn't lift with a shadow, but rather brighten in fill color or gain a "Level-up Gold" stroke.

Backdrop blurs (Glassmorphism) are reserved strictly for modal overlays to keep the focus on the current "Quest."

## Shapes
The shape language is **Soft (0.25rem)**. This provides a precise, technical feel that avoids the "playfulness" of overly rounded pills while remaining more modern and accessible than sharp 90-degree corners. 

Buttons and progress bars use the base `rounded` (4px), while larger card containers use `rounded-lg` (8px). Progress bars for the "66-day arc" should have flat ends to emphasize a continuous, unbreakable timeline.

## Components
- **Buttons:** Primary buttons use a solid "Growth Emerald" fill with white or dark navy text. Secondary buttons are "Ghost" style with a "Steel Gray" border.
- **XP Bars:** Thin, horizontal bars. The "unfilled" portion is a dark translucent gray; the "filled" portion uses a gradient from "Growth Emerald" to "Level-up Gold."
- **Quest Cards:** These are the primary containers. They feature a title, a "JetBrains Mono" stat label (e.g., "+50 XP"), and a high-contrast checkbox.
- **Streak Counters:** Large-format numbers using "Hanken Grotesk" Bold, highlighted with a "Level-up Gold" outer glow when a streak milestone (e.g., 7, 21, 66 days) is hit.
- **Checkboxes:** Larger than standard (min 24px) to serve as high-contrast touch targets. When checked, they should trigger a haptic-like visual expansion and color shift to Emerald.
- **Motivational Cards:** Editorial-style cards that use "Discipline Navy" backgrounds with "Level-up Gold" typography for daily quotes or "Arc" milestones.
- **Navigation:** A bottom-docked tab bar on mobile with clear, geometric icons and minimal labels using the `label-caps` typography.