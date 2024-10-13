![banner](docs/images/banner.png)

![Clojure](https://img.shields.io/badge/Clojure-%23Clojure.svg?style=for-the-badge&logo=Clojure&logoColor=Clojure)

# 🌐 Download Folder Organizer using Clojure and Ba·bash·ka

Do you have an unorganized downloads folder? Well, instead of taking 2 minutes to organize my own Downloads folder, I took a couple of hours to write a script to organize it for you!

## 💡 Motivation

In all honesty, I have done something similar, but in Python. I decided to redo this project, but this time in a new language I'm learning: Clojure!

I am a firm believer that a "downloads organizer" project is a great way to expose yourself to various coding problems:
- Writing a CLI interface (one which follows best practise)
- Reading and writing to a file to manage your program's settings and preferences
- Processing a list
- Exposure to an operating system
- Regex to manage file numbers (when you have duplicate files)
- User feedback (UX)
- Creative problem solving (like handling file extensions. This becomes tricky when you have edge cases like `foo.bar.txt`)
- High steaks (you can overwrite a very important file). Thus, you need to make yourself a "testing environment"

## 🛠 Software Setup

You will need a few things setup and running:
- **Clojure**: This [book by Alan](https://www.braveclojure.com/getting-started/) is an amazing recourse and will help you a lot to get your feet wet with Clojure
- **IDE:** I use Intellij's [Community Edition](https://www.jetbrains.com/idea/download/other.html) (although I've had success with the [Calva](https://calva.io) extension in VS Code)
- **Ba·bash·ka**: their [online book](https://book.babashka.org) has an amazing 'getting started' section. I used [scoop](https://scoop.sh) to install it on Windows. You'll see that I use the [shebang](https://en.wikipedia.org/wiki/Shebang_(Unix)) to run my scripts.
- **Ba·bash·ka REPL in IntelliJ (Optional):** Use [the guide from cursive](https://cursive-ide.com/userguide/babashka.html) to get your REPL up and running in IntelliJ

## 🚀 Running the Script

Start off by cloning the repo.

> ⚠ Don't save the repo in the same folder you want to organize... I haven't implemented any failsafe for the "I can't move myself" problem

Since I'm using the shebang, you can invoke the script by simply calling the file:


```bash
./script.clj -v
```

which will display the current version of the script

You can pass the help argument to view all available options:

```bash
./script.clj -h
```

```txt
Usage: ./<filename> <options>
Version: 1.0.0
  -i, --input-folder   C:\Users\<user>\Downloads              The input folder directory
  -s, --setup          C:\<path to script dir>\setup.yml      The path to the setup yml file
  -g, --generate-setup false                                  Generates a default setup.yml file
  -h, --help                                                  Show help
  -v, --version                                               Show version

```

To get started, we first need to generate a default `setup.yml` file. This file will store all the file preference we want.

```bash
./script.clj -g
```

This will generate a file and save it in the same directory as the script. You can also specify a custom location:

```bash
./script.clj -g -s './my-file-preference.yml'
```

This will create a **yml** file with the following format:

```yml
setup:
    Videos:
      - mp4
      - gif
    Documents:
      - docx
      - doc
      - pdf
      - txt
      - md
      - html
```

Where "Videos" and "Documents" are the names of the folders it will create (if they are not already created) in the target directory. You then specify the file extensions which will be moved into these folders. The file extensions are not case sensitive!

You can then run the file, as is, and all the default settings should be more than enough!

```bash
./script.clj
```

The default will target the users Download directory (for Windows) and use the `setup.yml` file in the script's directory.

If you are like me, and you want to play around with it before running it on your Downloads folder, you can specify which folder it should organize! So the script can work on more than just your Downloads folder!
I provided an [input template](./input_template) folder in this repo to play around with the script. Just rename it to "input" since this is ignored by git. 

Let's see what we have!

```bash
ls ./input
```

```txt
dog.csv  dog.doggie.jpg  dog.jpg  dog.pdf  dog.png  dog.svg  dog.xlsx  horse.txt  horse.webp  horses.pptx  horses.zip
```

We have a lot of good examples to work with! Now we can run:

```bash
./script.clj -i './input' -s './setup.yml'
ls ./input
```

```txt
ArchiveFiles  Documents  PowerPoints  Spreadsheets  images
```

As easy as that! 

## ❓ Duplicate Files

In case you were wondering, the script will never overwrite a file with the same name. Similar to what your browser does, it will add a number indicator for duplicate file names.

Let's say you have a bunch of photos of dogs. And you always save the file as `dog.png`. Right now, you have the following files in your "Images" folder:
- `dog.png`
- `dog (1).png`

We just downloaded another `dog.png` image and saved it in your Downloads folder. When you run the script, it will do the following:
- I really want to move `dog.png` into "Downloads/Images". Are there any files with the name `Downloads/Images/dog.png`?
- Yes! Let's add a number to it and try to save it as `Downloads/Images/dog (1).png`. Is that available? No?
- What about `Downloads/Images/dog (2).png`? Yes! Save it!

## ⏰ Running the script using Windows Task Scheduler

We went through all the effort to make this script, but now it will be nice if we have it do its thing in the background.
1. Open Windows Task Scheduler
2. In the Actions tab on the right, click "Create Task"
3. In the General tab, Give it a creative name like "Organize my life"
4. In the triggers tab, you can trigger it however you want, but I chose the "At log on" with all its default settings. It should run when I log into my laptop.
5. In the actions tab, make a new action. Choose "Start a Program". Browse the files and point to the batch file [scheduler.bat](./scheduler.bat). This does assume that you have a bash terminal up and running. You can use this Batch file to trigger it with whatever terminal you like to use.
6. Save the task
7. You can check if the task is working by clicking the "Run" button on the side.

---

Made with ❤️ by [Johandielangman](https://github.com/Johandielangman)