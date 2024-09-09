import pyautogui
import time
import logging
from PIL import Image, ImageOps

# Set up logging configuration
logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

def wait_until_image_visible(image_path, timeout=15, confidence=0.8, grayscale=False, check_interval=0.5):
    # Process and save images
    process_and_save_image(image_path)
    
    # Ensure image_path is a string
    if not isinstance(image_path, str):
        raise TypeError("image_path must be a string.")
    
    start_time = time.time()
    end_time = start_time + timeout
    
    # Endless loop to check for the image
    while True:
        current_time = time.time()
        
        # Check if the timeout has been exceeded
        if current_time > end_time:
            logging.warning(f"Waiting for image '{image_path}' to be visible timed out after {timeout} seconds.")
            raise Exception(f"Image '{image_path}' not found within {timeout} seconds.")
    
        try:
            # Check if the image is visible
            image_location = pyautogui.locateOnScreen(image_path, confidence=confidence, grayscale=grayscale)
            
            if image_location is not None:
                # Log the coordinates of the image
                logging.warning(f"Image '{image_path}' found at coordinate ({image_location.left}, {image_location.top})")
                return True
            
        except Exception as e:
            # Log the exception message if any exception occurs
            logging.debug(f"Exception occurred: {e}. Retrying...")
        
        # Sleep for the interval before checking again
        time.sleep(check_interval)

def process_and_save_image(image_path):
    try:
        # Open the original image
        original_image = Image.open(image_path)

        # Check and process image if mode is supported
        if original_image.mode in ('RGB', 'L'):
            # Inverted image
            inverted_image = ImageOps.invert(original_image.convert('RGB'))
            inverted_path = image_path.replace('.png', '_inverted.png')
            inverted_image.save(inverted_path)
            logging.info(f"Inverted image saved as '{inverted_path}'")

            # Grayscale image
            grayscale_image = ImageOps.grayscale(original_image)
            grayscale_path = image_path.replace('.png', '_grayscale.png')
            grayscale_image.save(grayscale_path)
            logging.info(f"Grayscale image saved as '{grayscale_path}'")

            # Solarized image
            solarized_image = ImageOps.solarize(original_image, threshold=128)
            solarized_path = image_path.replace('.png', '_solarized.png')
            solarized_image.save(solarized_path)
            logging.info(f"Solarized image saved as '{solarized_path}'")
        else:
            logging.info(f"Image mode '{original_image.mode}' not supported for processing. Skipping image '{image_path}'.")

    except Exception as e:
        # Handle exceptions and log a message without raising an error
        logging.error(f"Failed to process and save image: {e}")

def click_image_action(image_path, timeout=15, confidence=0.8, grayscale=False):
    # Ensure image_path is a string
    if not isinstance(image_path, str):
        raise TypeError("image_path must be a string.")

    # Wait until the image is visible
    if wait_until_image_visible(image_path, timeout, confidence, grayscale):
        # Locate the image center and click it
        location = pyautogui.locateCenterOnScreen(image_path, confidence=confidence, grayscale=grayscale)
        if location is not None:
            pyautogui.click(location)
            logging.info(f"Clicked on image '{image_path}'.")
        else:
            logging.error(f"Image '{image_path}' found but not clickable.")
            raise Exception(f"Image '{image_path}' found but not clickable.")
        
def run_installer(installer_path):
    pyautogui.hotkey('win', 'r')  # Open Run dialog
    time.sleep(10)
    pyautogui.typewrite(installer_path)  # Type the installer path
    pyautogui.press('enter')  # Press Enter to run the installer