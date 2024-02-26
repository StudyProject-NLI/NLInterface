## Features
- *SCROLLABLE GROCERY LIST*: A recycler view of cards, each holding the name of one grocery
item on the grocery list. The card width dynamically adapts to the length of the item
name. Greyed-out cards indicate that an item is in the cart.
- *ADD ITEM BUTTON*: A bright red button entitled ‘Add Item’ is displayed at the bottom of
the screen. If clicked, a dialog opens.
- *ADD ITEM DIALOG*: Opens when the add item button is clicked. The dialog consists of a
descriptive header entitled ‘Add a New Grocery Item,’ an editable text field with the hint
‘Item Name,’ a positive button ‘Add,’ and a negative button ‘Cancel.’ When the dialog is
opened, the keyboard is also activated. Clicking the positive button adds a new item by
the input name. The negative button cancels the action.

## Actions
- *ADDING ITEMS*: Items can be added to the list by voice command or via the ‘add item’
button and corresponding dialog window. Per default, a new item is not in the cart.
- *REMOVING ITEMS*: Items can be removed from the list by swiping right, left, or by voice
command.
- *PLACING ITEMS IN / REMOVING ITEMS FROM THE CART*: Long-clicking an item card changes the
in-cart status of the item. If it is not in the cart, i.e., not greyed out, it is placed in the
cart and greyed out. Else, it is taken out of the cart and colored. Alternatively, the in-
cart status can also be changed by voice command.
- *READING OUT AN ITEM NAME*: Clicking an item card will read out the item name.

## Voice Commands
- "GO TO [DESIRED ACTIVITY NAME]": Navigates to the desired activity.
- "ADD AN ITEM [ITEM NAME]": Adds a new item.
- "REMOVE AN ITEM [ITEM NAME]": Removes the item from the list if it exists.
- "ADD AN ITEM TO THE CART [ITEM NAME]": Adds the item to the cart if it exists and is not
already in the cart.
- "REMOVE AN ITEM FROM THE CART [ITEM NAME]": Removes the from the cart if it exists and
is in the cart.
- "CHECK IF AN ITEM IS ON THE LIST [ITEM NAME]": States whether the item is on the list.
- "LIST ALL GROCERY ITEMS": Reads out the names of all grocery items.
- "LIST ALL ITEMS IN THE CART": Reads out the names of all grocery items in the cart.
- "LIST ALL ITEMS NOT IN THE CART": Reads out the name of all grocery items not in the cart.
- "TELL ME MY OPTIONS": Reads out all possible voice commands for this activity.