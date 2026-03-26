# 🎥 Smart Surveillance System with Motion Detection
### Java + OpenCV + Swing GUI

---

## 📁 Folder Structure

```
SmartSurveillance/
│
├── src/                        ← All Java source code
│   ├── Main.java               ← Entry point (runs first)
│   ├── LoginDialog.java        ← Login popup window
│   ├── MainUI.java             ← Main application window (GUI)
│   ├── CameraHandler.java      ← Webcam management using OpenCV
│   ├── MotionDetector.java     ← Frame-differencing motion detection
│   ├── MotionLogger.java       ← Writes events to log file
│   └── AlertSound.java        ← Plays beep when motion detected
│
├── out/                        ← Compiled .class files (auto-created)
├── captured_images/            ← Auto-saved motion images (auto-created)
├── logs/                       ← Motion log text file (auto-created)
│
├── build_and_run.bat           ← Windows: double-click to build & run
├── build_and_run.sh            ← Linux/Mac: run in terminal
└── README.md                   ← This file!
```

---

## 🧠 How It Works

```
Webcam → Grab Frame → Convert to Grayscale → Apply Blur
       → Compare with Previous Frame (absdiff)
       → Count Different Pixels
       → If pixels > threshold → MOTION DETECTED!
       → Save image + Play sound + Write to log
```

---

## 🛠️ Setup Guide (Step-by-Step)

### Prerequisites
- Java JDK 8 or higher
- OpenCV 4.x (Java bindings)

---

### ✅ Step 1: Install Java JDK

**Windows:**
1. Go to: https://adoptium.net/
2. Download and install JDK 17 (LTS)
3. Check installation: open CMD → type `java -version`

**Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install default-jdk
java -version
```

---

### ✅ Step 2: Download OpenCV

**Windows:**
1. Go to: https://opencv.org/releases/
2. Download **OpenCV 4.10.0** (Windows version)
3. Run the `.exe` installer → Extract to `C:\opencv\`
4. Inside `C:\opencv\build\java\` you will find:
   - `opencv-4120.jar` ← Java bindings
   - `x64\opencv_java4100.dll` ← Native library

**Linux (Ubuntu) – Easiest method:**
```bash
sudo apt update
sudo apt install libopencv-dev
# Find where the JAR was installed:
find / -name "opencv*.jar" 2>/dev/null
```

**Linux (Build from Source – full control):**
```bash
# Install build tools
sudo apt install build-essential cmake git
sudo apt install libgtk-3-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev

# Download and build OpenCV with Java bindings
git clone https://github.com/opencv/opencv.git
cd opencv
mkdir build && cd build
cmake -DBUILD_SHARED_LIBS=OFF -DBUILD_opencv_java=ON ..
make -j4
# Find JAR in: build/bin/opencv-4120.jar
# Find .so in: build/lib/libopencv_java4120.so
```

---

### ✅ Step 3: Clone / Download This Project

Download or copy the `SmartSurveillance/` folder to your computer.

---

### ✅ Step 4: Update Build Script with Your OpenCV Path

**On Windows** – open `build_and_run.bat` in Notepad and change:
```bat
SET OPENCV_DIR=C:\opencv\build\java
SET OPENCV_VERSION=4100
```

**On Linux/Mac** – open `build_and_run.sh` and change:
```bash
OPENCV_DIR="/path/to/opencv/build/bin"
OPENCV_VERSION="4100"
```

---

### ✅ Step 5: Build and Run

**Windows:**
- Double-click `build_and_run.bat`
- OR open CMD in the project folder and run:
  ```cmd
  build_and_run.bat
  ```

**Linux/Mac:**
```bash
chmod +x build_and_run.sh
./build_and_run.sh
```

**Manual compile/run (if scripts don't work):**
```bash
# Compile
javac -cp "/path/to/opencv.jar" -d out src/*.java

# Run (Windows uses ; as separator, Linux/Mac use :)
java -cp "out;/path/to/opencv.jar" -Djava.library.path="/path/to/opencv/x64" Main
```

---

## 🖥️ How to Use the Application

| Action | Steps |
|--------|-------|
| **Login** | Username: `admin` / Password: `1234` |
| **Start Camera** | Click ▶ Start Camera button |
| **View Live Feed** | Video appears in the left panel |
| **Motion Detected** | Status turns RED + image auto-saved + beep plays |
| **Manual Capture** | Click 📷 Capture Image button anytime |
| **Adjust Sensitivity** | Use the slider (left = more sensitive) |
| **Toggle Sound** | Check/uncheck "Enable Alert Sound" |
| **Stop Camera** | Click ⏹ Stop Camera |
| **Exit** | Close window → confirm exit |

---

## 📂 Output Files

| File/Folder | Description |
|-------------|-------------|
| `captured_images/` | All auto-saved + manual images |
| `captured_images/2024-06-15_14-32-07.jpg` | Timestamp-named images |
| `logs/motion_log.txt` | All events logged here |

**Sample log file:**
```
[2024-06-15 14:32:07] INFO: Application started. Login successful.
[2024-06-15 14:32:15] INFO: Camera started.
[2024-06-15 14:32:31] MOTION DETECTED – Image saved: captured_images/2024-06-15_14-32-31.jpg
[2024-06-15 14:34:01] INFO: Manual capture: captured_images/2024-06-15_14-34-01.jpg
[2024-06-15 14:35:00] INFO: Camera stopped.
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| `UnsatisfiedLinkError` | Native OpenCV library (.dll/.so) not found. Check `-Djava.library.path` |
| `Camera not found` | Check webcam is connected; try device index 1 instead of 0 |
| `java.lang.NoClassDefFoundError` | OpenCV JAR not in classpath. Check `-cp` argument |
| No motion detected | Increase sensitivity (move slider left) |
| Too many false detections | Decrease sensitivity (move slider right) |
| Blank video panel | Camera opened but frames are empty – try restarting |

---

## 👨‍💻 Technologies Used

- **Java SE** – Core language
- **Java Swing** – GUI framework
- **OpenCV 4.x** – Computer vision (frame capture, image processing)
- **javax.sound.sampled** – Audio generation (no external file needed)

---

*Built for | Java + OpenCV | Motion Detection Surveillance*
