# Natural Language Interface

This is the code repository for the Android app developed in the studyproject Sensory augmentation and grasping movements.

[![Testing APK](https://github.com/StudyProject-NLI/NLInterface/actions/workflows/debug_build.yml/badge.svg)](https://github.com/StudyProject-NLI/NLInterface/actions/workflows/debug_build.yml)

# Roadmap

```mermaid
	gitGraph LR:
		commit id: "Current state"
		commit id: "Workgroup Release" tag: "v0.0.1"
		commit id: "Run example tensorflow model"
		commit id: "Feedback round with experimental group"
		commit id: "Integrate feedback"  tag: "v0.0.2"
		commit id: "Christmas break"
		commit id: "Object detection via any image input"
		commit id: "Polishing"
		commit id: "Optivist Presentation" tag: "v0.0.3"
		commit id: "Prepare for codebase transfer"
```

# Documentation

## Running locally
- Install Python
- `pip install -r docs/requirements.txt`

Run the documentation server via 
```bash
$ mkdocs serve
```


# Building the app locally
Requirements:
- At least Java 8 JDK installed
- Create a `local.properties` file at the root of this repository with the following content

```bash
MAPS_API_KEY=your_google_maps_api_key
```

You can request your keys via the [Google Maps Platform](https://developers.google.com/maps/documentation/embed/get-api-key).

Then build the application with 

```
./gradlew build -x lint -x lintVitalRelease
```

on MacOS or Linux based operating systems.

The resulting installable application can be found under 

`app/build/outputs/apk/debug/app-debug.apk`

which you can copy and install on your device.
