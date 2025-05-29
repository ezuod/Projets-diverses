# Projet client-serveur

## Project Description

The project aims to develop an application that manages a set of images on a server, indexes those images, and allows users to search for similar images based on a given/selected image. The system consists of a **server** and a **client** that communicate with each other.

---

## Run Project

1. **Configure your environment**  
   Make sure to check out the `application.properties` file and configure it according to your setup.

2. **Install Python dependencies**

   The image analysis service requires the following Python libraries:

   ```
   pip install fastapi uvicorn transformers keybert pillow torch
   ```

   > If you're using a GPU, install the appropriate version of PyTorch with CUDA support from [https://pytorch.org](https://pytorch.org).

   > For Apple Silicon, you can install the optimized PyTorch version with Metal backend support from [https://developer.apple.com/metal/pytorch/](https://developer.apple.com/metal/pytorch/).

3. **Start the project**

   ```
   # From the root directory
   uvicorn analyze.analyze_service:app --reload --reload-dir analyze

   # Then start the Spring Boot backend
   mvn clean install
   mvn --projects backend spring-boot:run
   ```

   > Make sure the `analyze/` folder contains an `__init__.py` file so Python treats it as a package.

---

## Operating Systems

The server has been tested on the following operating systems:

- Debian Bookworm
- Windows
- macOS 

The server works correctly on these platforms, with no major issues observed.

## Web Browsers

The client application has been tested on the following web browsers:

- Google Chrome
- Safari
- Firefox

The web client works correctly on these browsers. Older versions may encounter compatibility issues.