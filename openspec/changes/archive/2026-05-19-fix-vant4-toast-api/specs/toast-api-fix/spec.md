# Toast API Fix (spec-level: no behavioral change)

This change is a pure API compatibility fix. No spec-level behavior is added, modified, or removed.

The Toast component's external behavior (display text, timing, appearance, positioning) remains identical. Only the internal function call signature changes from the deprecated Vant 2/3 style (`Toast.success()` / `Toast.fail()`) to the Vant 4 style (`showSuccessToast()` / `showFailToast()`).

## ADDED Requirements

None — this is a bugfix with no new requirements.

## MODIFIED Requirements

None — existing spec behavior is unchanged.

## REMOVED Requirements

None.
