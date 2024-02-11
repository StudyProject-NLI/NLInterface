## Features
- *SCROLLABLE PLACE DETAILS LIST*: A recycler view of place detail cards, each holding the
name of a store and displaying that store’s opening hours (for the current day).
- *FAVORITE ICON*: A star icon next to each place detail card indicates whether the place is a
favorite. Filled-in icons are favorites; outlines are not.
- *SEARCH BAR*: A button layout provided by Google. Clicking it will open the place search
dialog. It has been placed underneath the voice activation button, as its layout cannot
be changed and is unsuitable for visually impaired and blind people. The voice activation
button acts as a reference point.
- *PLACE SEARCH DIALOG*: A dialog provided by Google that searches the Google Places API
for matches for a given input string. While typing, the dialog will attempt to
autocomplete the input string by displaying a list of possible options.

## Actions
- *ADDING NEW PLACES*: Places can currently only be added by clicking the search bar button
and then entering a search string in the editable text field of the place search dialog.
This place will be added to the place details list by clicking on a suggested match.
- *DELETING PLACES*: Places can be removed from the list by swiping right, left, or by voice
command.
- *READING OUT AN ITEM NAME*: Clicking a place detail card will read out the place name and
today’s opening hours.
- *ADDING PLACES TO / REMOVING PLACES FROM FAVORITES*: A place can be added to favorites
either by voice command or by clicking the favorite icon outline of the place. Clicking a
filled-in favorite icon will remove the corresponding place from favorites, which can also
be performed by voice command.

## Voice Commands
- "GO TO [DESIRED ACTIVITY NAME]": Navigates to the desired activity.
- "REMOVE A PLACE [ITEM NAME]": Removes the place if it exists.
- "TELL ME THE OPENING HOURS OF A PLACE [ITEM NAME]": Reads out today’s opening hours of
the place, if it exists.
- "ADD A PLACE TO FAVORITES [ITEM NAME]": Adds the place to favorites if it exists and is
not already a favorite.
- "REMOVE A PLACE FROM FAVORITES [ITEM NAME]": Removes the place from favourites if it
exists and is a favorite.
- "LIST ALL SAVED PLACES": Reads out the names of all places on the place detail list.
- "LIST MY FAVORITE PLACES": Reads out the names of all favorite places on the place detail
list.
- "LIST ALL OPEN PLACES": Reads out the names of all currently open places on the place
detail list.
- "TELL ME MY OPTIONS": Reads out all possible voice commands for this activity.