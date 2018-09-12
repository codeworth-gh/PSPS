# PSPS

**Very very initial documentation**

## What is this?

## What does it give me?

### Site sections

Using the enums and page trees in `views.Helpers`.

### Success/Fail/Warn Messages

Are passed over the `flash` scope, with the key `FlashKeys.MESSAGE`. They are automatically
shown by the `bsBase` template. You can either pass a String (which will result in an "info"
message), or a full-fledged `Informational`.

### Playjax

JS Library to make JS6 `fetch` calls using Play's JS Routers. Uses a fluent syntax for getting the route.
Supports CSRF, by adding an HTML element with id `Playjax_csrfTokenValue` and value
of the current CSRF token.

### Smaller issues
* *Localization support* Using standard Play localization.
* Separate front-end and back-office JS Routers.
* Convenience methods for template rendering in `views.Helpers`.
