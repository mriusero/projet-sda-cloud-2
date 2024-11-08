import sys
import json
import requests
import base64
import logging

logging.basicConfig(filename='error.log', level=logging.ERROR)

API_KEY = 'meow'

def analyze_image(image_url):
    api_url = f'https://vision.googleapis.com/v1/images:annotate?key={API_KEY}'
    try:
        response = requests.get(image_url)
        response.raise_for_status()
        image_content = base64.b64encode(response.content).decode('utf-8')

        request_payload = {
            'requests': [
                {
                    'image': {
                        'content': image_content
                    },
                    'features': [
                        {
                            'type': 'LABEL_DETECTION',
                            'maxResults': 5
                        }
                    ]
                }
            ]
        }

        response = requests.post(api_url, json=request_payload)
        response.raise_for_status()

        result = response.json()
        labels = [label['description'] for label in result['responses'][0].get('labelAnnotations', [])]
        return labels
    except Exception as e:
        logging.error(f"An error occurred: {e}")
        return []

def main():
    if len(sys.argv) != 2:
        logging.error("Usage: python analyze_image.py <image_url>")
        sys.exit(1)

    image_url = sys.argv[1]
    labels = analyze_image(image_url)
    print(json.dumps(labels, indent=2))

if __name__ == "__main__":
    main()
