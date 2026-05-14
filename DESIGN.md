# Design System — 因私卡券管理系统

## Product Context
- **What this is:** 企业内部因私卡券管理工具——员工申请使用公司资源 → 审批通过 → 获得电子券 → 凭券使用 → 核销记录
- **Who it's for:** 行政/管理员(PC管理端) + 普通员工(H5移动端)
- **Space/industry:** 企业内部审批与资源管理
- **Project type:** 企业后台(PC) + 移动工具(H5)

## Aesthetic Direction
- **Direction:** Industrial/Utilitarian — 功能优先，零装饰
- **Decoration level:** minimal — 排版完成所有视觉工作
- **Mood:** 可靠、精确、冷静。像银行系统一样可信，每个像素都有存在理由
- **Memorable thing:** "这个系统不会出错" — 核销记录精确、券码不重复、审计链路完整

## Typography
- **Global:** PingFang SC, Microsoft YaHei, -apple-system, sans-serif
- **Voucher Codes / Data Tables / Numbers:** JetBrains Mono, SF Mono, Consolas, Monaco, monospace
- **Loading:** Google Fonts (JetBrains Mono) or self-hosted via npm `@fontsource/jetbrains-mono`
- **Scale:**
  - 11px: captions, badge labels
  - 12px: secondary text, table cells, code inline
  - 14px: body, form labels, menu items
  - 16px: card titles, button text
  - 20px: page headings
  - 24px: section titles
  - 28px: hero/product name

## Color
- **Approach:** restrained — 颜色稀有且有意义，仅状态标签和主操作使用彩色
- **Primary:** #1989fa — 主操作按钮、链接、选中态、焦点环
- **Primary Hover:** #40a9ff
- **Primary Active:** #096dd9
- **Sidebar:** #0a1628 — 深海军蓝侧边栏，比标准Ant Design深30%
- **Semantic:**
  - Success: #07c160 (核销通过、操作成功)
  - Warning: #fa8c16 (即将过期)
  - Error: #ee0a24 (核销失败、已过期、已作废)
- **Neutrals:**
  - Page Background: #f7f8fa
  - Card Background: #ffffff
  - Border: #ebedf0
  - Border Light: #f5f5f5
  - Text Primary: #323233
  - Text Secondary: #969799
  - Text Placeholder: #c8c9cc
- **Dark mode:** 不适用（内部系统，仅浅色模式）

## Spacing
- **Base unit:** 8px
- **PC Admin:** compact密度 — 数据密集场景
  - 2xs: 4px, xs: 8px, sm: 12px, md: 16px, lg: 24px, xl: 32px, 2xl: 48px, 3xl: 64px
- **H5 Employee:** comfortable密度 — 触控目标≥44px
  - 2xs: 4px, xs: 8px, sm: 12px, md: 16px, lg: 24px
- **Touch targets:** 所有可点击元素最小44×44px

## Layout
- **Approach:** grid-disciplined — PC侧边栏+内容区，H5单列卡片流
- **PC Admin:**
  - Sidebar: 200px fixed width
  - Content: fluid, max-width 1400px
  - Breakpoints: 1920px (full), 1366px (compact sidebar 64px collapsed)
- **H5 Employee:**
  - Max width: 375px (base), fluid up to 768px
  - Single column, no horizontal scroll
- **Border radius:** sm: 4px (buttons, inputs, badges), md: 8px (cards), lg: 12px (modals)
- **Shadow:**
  - sm: 0 1px 2px rgba(0,0,0,.04) — subtle elevation
  - md: 0 2px 8px rgba(0,0,0,.06) — card elevation
  - lg: 0 4px 16px rgba(0,0,0,.08) — modal/dropdown

## Motion
- **Approach:** minimal-functional — 仅辅助理解的过渡动画
- **Easing:** enter: ease-out, exit: ease-in, move: ease-in-out
- **Duration:**
  - micro: 100ms (hover state, focus ring)
  - short: 200ms (button press, badge change)
  - medium: 300ms (page transition, card expand)
- **No:** decorative animations, scroll-driven effects, bounce, parallax

## Component Patterns

### Buttons
- Primary: `background: #1989fa; color: #fff; height: 40px; padding: 0 24px; border-radius: 4px;`
- Default: `background: #f7f8fa; border: 1px solid #ebedf0;`
- Sizes: sm(32px), md(40px), lg(48px)
- Disabled: opacity 0.5, cursor not-allowed

### Status Badges
- 有效: `background: #e8f8e8; color: #07c160`
- 已使用/已核销: `background: #e8f4ff; color: #1989fa`
- 已过期: `background: #fff7e6; color: #fa8c16`
- 已作废: `background: #fef0f0; color: #ee0a24`

### Input
- Default: `border: 1px solid #ebedf0; border-radius: 4px; height: 40px;`
- Focus: `border-color: #1989fa; box-shadow: 0 0 0 2px rgba(25,137,250,.15);`
- Voucher Code: `font-family: JetBrains Mono; letter-spacing: 2px; font-size: 20px;`
- Labels: always visible above fields, NOT placeholder-as-label

### Cards
- `background: #fff; border-radius: 8px; border: 1px solid #ebedf0; padding: 16px;`
- No decorative gradients, no colored left-borders

### QR Code
- Client-side rendering: qrcode.js
- Static content: voucher_code (Snowflake+Base32 encoded)
- Min display size: 160×160px on H5, clearly scannable

## Anti-Slop Rules
- ❌ No purple/violet gradients
- ❌ No 3-column feature grids with icons in colored circles
- ❌ No centered everything
- ❌ No bubbly border-radius on everything
- ❌ No decorative blobs or wavy SVG dividers
- ❌ No emoji as design elements
- ❌ No colored left-borders on cards
- ❌ No generic hero copy
- ❌ No system-ui / -apple-system as primary display font

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-05-13 | Initial design system created | /design-consultation based on office-hours + eng-review + design-review context |
| 2026-05-13 | Primary color #1989fa | /plan-design-review decision — blue trust signal for enterprise |
| 2026-05-13 | Sidebar #0a1628 deep navy | Risk choice — darker than standard for stronger visual anchor |
| 2026-05-13 | JetBrains Mono for voucher codes | Risk choice — IDE font in enterprise tool signals code=precision |
| 2026-05-13 | PingFang SC global font | /plan-design-review decision — clean Chinese rendering |
| 2026-05-13 | PC sidebar + H5 single column | /plan-design-review decision — enterprise standard |
| 2026-05-13 | 8px spacing base | Enterprise UI convention, compatibility with Element Plus |
