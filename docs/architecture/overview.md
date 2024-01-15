# How is our app structured?

```mermaid
flowchart
	subgraph activities
	m[MainActivity]
	m --> GroceryListActivity
	m --> PlaceActivity
	m --> SettingsActivity
	
	end
	subgraph utilities
	TextToSpeechUtility
	SpeechToTextButton
	SpeechToTextUtility
	
	end
```