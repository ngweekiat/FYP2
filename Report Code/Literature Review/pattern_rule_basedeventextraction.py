import re
from collections import defaultdict

# Sample text
text = """
On March 20, 2025, John Doe will attend a conference in New York. 
The conference, titled 'AI and the Future', will take place at 10 AM at the Grand Hall.
"""

# Define event patterns (trigger words for events)
event_patterns = {
    "conference": ["conference", "meeting", "seminar", "webinar"],
    "appointment": ["appointment", "meeting", "schedule"],
}

# Define a list of date-related patterns
date_patterns = [r"\b(?:\d{1,2}[/-]\d{1,2}[/-]\d{4}|\b(?:January|February|March|...)\s\d{1,2},\s\d{4})\b"]

# Define location keywords
location_keywords = ["at", "in", "on"]

# Function to extract events based on patterns
def extract_events(text):
    events = defaultdict(list)
    
    # Extract dates using regex
    dates = re.findall(r"\b(?:\d{1,2}[/-]\d{1,2}[/-]\d{4}|\b(?:January|February|March|...)\s\d{1,2},\s\d{4})\b", text)
    
    # Loop through predefined event patterns and find matches
    for event_type, triggers in event_patterns.items():
        for trigger in triggers:
            matches = re.findall(rf"\b{trigger}\b", text, re.IGNORECASE)
            if matches:
                events[event_type].append({"trigger": trigger, "dates": dates})
    
    # Extract location mentions
    locations = []
    for keyword in location_keywords:
        if keyword in text:
            location = text.split(keyword)[-1].strip()
            locations.append(location)
    
    return events, locations

# Call the event extraction function
events, locations = extract_events(text)

# Print extracted events and locations
print("Extracted Events:")
for event_type, details in events.items():
    for detail in details:
        print(f"Event: {event_type}, Trigger: {detail['trigger']}, Dates: {detail['dates']}")

print("\nExtracted Locations:")
for location in locations:
    print(f"Location: {location}")
