# PSPS

**Very very initial documentation**

## Site sections

Using the enums and page trees in `views.Helpers`.

## Success/Fail/Warn Messages

Are passed over the `flash` scope, with the key `FlashKeys.MESSAGE`. They are automatically
shown by the `bsBase` template. You can either pass a String (which will result in an "info"
message), or a full-fledged `Informational`.
