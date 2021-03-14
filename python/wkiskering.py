import numpy as np
import cv2

def original_whiskering(frame, obstacleList, obstacleClasses, threshold):
    """Performs the basic whiskering algorithm w/o the depth map stuff

    Gets the weighted average of all the obstacles and reflects across the middle of the frame to get direction of travel

    Arguments:
        frame {unit8 array} -- frame from the camera (not depth map image)
        obstacleList {list of obstacles detected} -- list of strings for each obstacle. Correspond to same indices as obstacleClasses
        obstacleClasses {list of tuples} -- list of 4x1 tuples. Tuple has coordinates for each rectangle.
                                            Format of tuple is (startX, startY, endX, endY)
        threshold {int} -- threshold for which we consider an object a priority; usually 10 feet

    Returns:
        unit8 array -- frame with stuff drawn on it
    """
    # Get frame dimensions
    (screen_height, w) = frame.shape[:2]

    # Calculate the middle of the screen (one to reflect on)
    screen_mid = w // 2

    # Start iterating for every obstacle
    for index, obstacle in enumerate(obstacleClasses):
        # Extract coordinates for each obstacle
        (startX, endX, startY, endY) = obstacleList[index]

        # Mid point of bounding box
        x_mid = round((startX + endX) / 2, 4)
        y_mid = round((startY + endY) / 2, 4)

        # Calculate height
        height = round(endY - startY, 4)

        # Distance from camera based on triangle similarity. Divide by 30.48 to get feet
        distance = ((165 * F) / height) / 30.48

        # Add obstacle into array
        if (distance < threshold):
            # Red and blue distance correspond to the red and blue lines drawn on the frame
            red_dist = int(x_mid) - screen_mid
            blue_dist = sqrt(pow((int(x_mid) - screen_mid), 2) + pow((int(y_mid) - h), 2))
            yx_angle = math.asin(red_dist / blue_dist) * (180 / np.pi)

            # Draw lines on the frame
            cv2.line(frame, (screen_mid, 0), (screen_mid, h), (255, 255, 255), 10) # <-- white line in middle
            cv2.line(frame, (int(x_mid), int(y_mid)), (screen_mid, int(y_mid)), (0, 0, 255), 10) # <-- red line
            cv2.line(frame, (int(x_mid), int(y_mid)), (screen_mid, h), (255, 0, 0), 10) # <-- blue line

            # Calculate the vector and add it to the array of high priority objects
            vector = (x_mid - screen_mid, y_mid - h, distance, yx_angle)
            vectorArray.append(vector)

    # Calculate x and y average vectors and take the negative of them
    x_arr = [x / distance for x, y, distance, yx_angle in vectorArray]
    y_arr = [y / distance for x, y, distance, yx_angle in vectorArray]
    avg_x = -1 * mean(x_arr) if x_arr else 0
    avg_y = -1 * mean(y_arr) if y_arr else 0

    # Draw black line
    cv2.line(frame, (int(avg_x) + screen_mid, int(avg_y)), (screen_mid, h), (0, 0, 0), 20)
    return frame

def depthMap_whiskering(frame, obstacleList, obstacleClasses, threshold, depthImage):
    """Performs whiskering with the depth map

    Gets the weighted average of all the obstacles and reflects across the middle of the frame to get direction of travel

    Arguments:
        frame {unit8 array} -- frame from the camera (not depth map image)
        obstacleList {list of obstacles detected} -- list of strings for each obstacle. Correspond to same indices as obstacleClasses
        obstacleClasses {list of tuples} -- list of 4x1 tuples. Tuple has coordinates for each rectangle.
                                            Format of tuple is (startX, startY, endX, endY)
        threshold {int} -- threshold for which we consider an object a priority; usually 10 feet
        depthImage {unit8} -- depth map for the same frame

    Returns:
        unit8 array -- frame with stuff drawn on it
    """
    # Get frame dimensions
    (screen_height, w) = frame.shape[:2]

    # Calculate the middle of the screen (one to reflect on)
    screen_mid = w // 2

    # Start iterating for every obstacle
    for index, obstacle in enumerate(obstacleClasses):
        # Extract coordinates for each obstacle
        (startX, endX, startY, endY) = obstacleList[index]

        # Mid point of bounding box
        x_mid = round((startX + endX) / 2, 4)
        y_mid = round((startY + endY) / 2, 4)

        # Calculate height
        height = round(endY - startY, 4)

        # Get the portion of the image from depth map
        boxed = disparity[startY:endY, startX: endX]

        # Calculate distance
        averageDistance = np.mean(boxed)

        # Add obstacle into array
        if (averageDistance < threshold):
            # Red and blue distance correspond to the red and blue lines drawn on the frame
            red_dist = int(x_mid) - screen_mid
            blue_dist = sqrt(pow((int(x_mid) - screen_mid), 2) + pow((int(y_mid) - h), 2))
            yx_angle = math.asin(red_dist / blue_dist) * (180 / np.pi)

            # Draw lines on the frame
            cv2.line(frame, (screen_mid, 0), (screen_mid, h), (255, 255, 255), 10) # <-- white line in middle
            cv2.line(frame, (int(x_mid), int(y_mid)), (screen_mid, int(y_mid)), (0, 0, 255), 10) # <-- red line
            cv2.line(frame, (int(x_mid), int(y_mid)), (screen_mid, h), (255, 0, 0), 10) # <-- blue line

            # Calculate the vector and add it to the array of high priority objects
            vector = (x_mid - screen_mid, y_mid - h, averageDistance, yx_angle)
            vectorArray.append(vector)

    # Calculate x and y average vectors and take the negative of them
    x_arr = [x / distance for x, y, distance, yx_angle in vectorArray]
    y_arr = [y / distance for x, y, distance, yx_angle in vectorArray]
    avg_x = -1 * mean(x_arr) if x_arr else 0
    avg_y = -1 * mean(y_arr) if y_arr else 0

    # Draw black line
    cv2.line(frame, (int(avg_x) + screen_mid, int(avg_y)), (screen_mid, h), (0, 0, 0), 20)

    return frame
