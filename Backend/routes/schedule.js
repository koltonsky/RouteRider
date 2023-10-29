
const {MongoClient, ObjectId} = require('mongodb');
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

const createNewSchedule = async (req, res) => {
  try {
    // Extract user data from the request body
    const scheduleData = req.body;

    // Assuming you have already connected to the MongoDB client
    const collection = client.db('ScheduleDB').collection('schedulelist');

    // Check if a user with a specific identifier (e.g., email) already exists
    const existingSchedule = await collection.findOne({ email: scheduleData.email });

    if (existingSchedule) {
      // If a user with the same email exists, return an error message
      const errorMessage = 'Schedule with this email already exists';
      const errorMessageLength = Buffer.byteLength(errorMessage, 'utf8');
      res.set('Content-Length', errorMessageLength);
      res.status(109).json({ message: errorMessage });
    } else {
      // If the user doesn't exist, insert the new user document into the collection
      const insertResult = await collection.insertOne(scheduleData);
      const successMessage = 'Schedule created successfully';
      const successMessageLength = Buffer.byteLength(successMessage, 'utf8');
      res.set('Content-Length', successMessageLength);
      res.status(201).json({ message: successMessage });
    }

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};


  const getScheduleByEmail = async (req, res) => {
    try {
      // Extract the user's email from the request parameters
      const userEmail = req.params.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');

      console.log(userEmail);
  
      // Find the schedule by their email
      const schedule = await collection.findOne({ email: userEmail });
  
      if (schedule) {
        // User found, send user information as a response
        res.status(200).json("Schedule found");
        
      } else {
        // User not found
        res.status(404).json({ error: 'Schedule not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  const updateSchedule = async (req, res) => {
    try {
      // Extract the email from the URL parameter
      const email = req.params.email;
  
      // Extract the updated schedule data from the request body
      const updatedScheduleData = req.body;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
  
      // Check if a schedule with the specified email exists
      const existingSchedule = await collection.findOne({ email });
  
      if (!existingSchedule) {
        // If a schedule with the specified email doesn't exist, return an error message
        res.status(404).json({ message: 'Schedule not found' });
      } else {
        // Update the schedule document in the collection
        const updateResult = await collection.updateOne({ email }, { $set: updatedScheduleData });
  
        if (updateResult.modifiedCount > 0) {
          res.status(200).json({ message: 'Schedule updated successfully' });
        } else {
          res.status(204).json({ message: 'No changes made to the schedule' });
        }
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
/*
  const editEventByID = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventId = req.params.id; // Add 'id' parameter
      const updatedEvent = req.body; // New event data to replace the existing event
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      const eventToUpdate = schedule.events.find(event => event.id === eventId);
  
      if (!eventToUpdate) {
        res.status(400).json({ error: 'Event not found in the schedule' });
        return;
      }
  
      // Update the event based on its _id
      Object.assign(eventToUpdate, updatedEvent);
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
*/

/*
const editEventByID = async (req, res) => {
  try {
    const userEmail = req.params.email;
    const eventId = req.params.id;
    const updatedEvent = req.body;

    const collection = client.db('ScheduleDB').collection('schedulelist');
    const schedule = await collection.findOne({ email: userEmail });

    if (!schedule) {
      res.status(404).json({ error: 'Schedule not found' });
      return;
    }

    const eventToUpdateIndex = schedule.events.findIndex(event => event.id === eventId);

    if (eventToUpdateIndex === -1) {
      res.status(400).json({ error: 'Event not found in the schedule' });
      return;
    }

    // Remove the event to be updated from the array
    const [eventToUpdate] = schedule.events.splice(eventToUpdateIndex, 1);

    // Update the event based on its _id
    Object.assign(eventToUpdate, updatedEvent);

    // Find the index to insert the updated event based on startTime
    const insertIndex = schedule.events.findIndex(event => new Date(eventToUpdate.startTime) < new Date(event.startTime));

    // Insert the updated event at the calculated index
    schedule.events.splice(insertIndex, 0, eventToUpdate);

    const updateResult = await collection.updateOne(
      { _id: schedule._id },
      { $set: { events: schedule.events } }
    );

    if (updateResult.modifiedCount > 0) {
      res.status(200).json({ message: 'Event updated successfully' });
    } else {
      res.status(500).json({ error: 'Failed to update event' });
    }
  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};
*/
/*
const editEventByID = async (req, res) => {
  try {
    const userEmail = req.params.email;
    const eventId = req.params.id;
    const newEvent = req.body; // Assuming the new event data is sent in the request body

    const collection = client.db('ScheduleDB').collection('schedulelist');
    const schedule = await collection.findOne({ email: userEmail });

    if (!schedule) {
      res.status(404).json({ error: 'Schedule not found' });
      return;
    }

    if (eventId) {
      // Editing an existing event
      const eventToUpdateIndex = schedule.events.findIndex(event => event.id === eventId);

      if (eventToUpdateIndex === -1) {
        res.status(400).json({ error: 'Event not found in the schedule' });
        return;
      }

      // Remove the event to be updated from the array
      const [eventToUpdate] = schedule.events.splice(eventToUpdateIndex, 1);

      // Update the event based on its _id
      Object.assign(eventToUpdate, newEvent);

      // Find the index to insert the updated event based on both startTime and endTime
      const insertIndex = schedule.events.findIndex(event => {
        return (
          new Date(eventToUpdate.startTime) <= new Date(newEvent.startTime) &&
          new Date(eventToUpdate.endTime) <= new Date(newEvent.endTime)
        );
      });

      // Insert the updated event at the calculated index
      schedule.events.splice(insertIndex, 0, eventToUpdate);
    } else {
      // Adding a new event
      // Function to compare events based on both startTime and endTime
      function compareEvents(event1, event2) {
        if (new Date(event1.startTime) < new Date(event2.startTime)) return -1;
        if (new Date(event1.startTime) > new Date(event2.startTime)) return 1;
        if (new Date(event1.endTime) < new Date(event2.endTime)) return -1;
        if (new Date(event1.endTime) > new Date(event2.endTime)) return 1;
        return 0;
      }

      // Add the new event to the events array and sort by both startTime and endTime
      schedule.events.push(newEvent);
      schedule.events.sort(compareEvents);
    }

    const updateResult = await collection.updateOne(
      { _id: schedule._id },
      { $set: { events: schedule.events } }
    );

    if (updateResult.modifiedCount > 0) {
      res.status(200).json({ message: 'Event updated successfully' });
    } else {
      res.status(500).json({ error: 'Failed to update event' });
    }
  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};
*/






  const addEvent = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const newEvent = req.body; // Assuming the new event data is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      // Function to compare events based on their start times
      function compareEvents(event1, event2) {
        return new Date(event1.startTime) - new Date(event2.startTime);
      }
  
      // Add the new event to the events array and sort by start time
      schedule.events.push(newEvent);
      schedule.events.sort(compareEvents);
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event added successfully' });
      } else {
        res.status(500).json({ error: 'Failed to add event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
  
/*
  const addEvent = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const newEvent = req.body; // Assuming the new event data is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      // Add the new event to the end of the events array
      schedule.events.push(newEvent);
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event added successfully' });
      } else {
        res.status(500).json({ error: 'Failed to add event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */
  
  /*
  const insertEventAtIndex = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const newEvent = req.body; // Assuming the new event data is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex > schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      // Insert the new event at the specified index
      schedule.events.splice(eventIndex, 0, newEvent);
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event inserted successfully' });
      } else {
        res.status(500).json({ error: 'Failed to insert event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  const editEventAtIndex = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedEvent = req.body; // New event data to replace the existing event
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      // Update the event at the specified index
      schedule.events[eventIndex] = updatedEvent;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */
  
  

  /*
  const editEventByIndex = async (req, res) => {
    try {
      // Extract user's email, event index, and updated event data from the request
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index); // Assuming index is provided as a parameter
      const updatedEvent = req.body; // Assuming the updated event data is sent in the request body
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their email
      const user = await collection.findOne({ email: userEmail });
  
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      // Check if the event index is valid
      if (eventIndex < 0 || eventIndex >= user.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      // Update the event data by index
      user.events[eventIndex] = updatedEvent;
  
      // Update the document in the collection to reflect the change
      const updateResult = await collection.updateOne(
        { _id: user._id },
        { $set: { events: user.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

  /*
  const editEventName = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedName = req.body.eventName; // Assuming the updated name is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      schedule.events[eventIndex].eventName = updatedName;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event name updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event name' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  const editEventAddress = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedAddress = req.body.address; // Assuming the updated name is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      schedule.events[eventIndex].address = updatedAddress;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event address updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event address' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  /*
  const editEventDate = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedDate = req.body.date; // Assuming the updated name is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      schedule.events[eventIndex].date = updatedDate;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event Date updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event date' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */


  const editEventGeolocation = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedGeolocation = req.body.geolocation; // Assuming the updated geolocation is sent as an array in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      if (Array.isArray(updatedGeolocation) && updatedGeolocation.length === 2) {
        schedule.events[eventIndex].geolocation = updatedGeolocation;
  
        const updateResult = await collection.updateOne(
          { _id: schedule._id },
          { $set: { events: schedule.events } }
        );
  
        if (updateResult.modifiedCount > 0) {
          res.status(200).json({ message: 'Event geolocation updated successfully' });
        } else {
          res.status(500).json({ error: 'Failed to update event geolocation' });
        }
      } else {
        res.status(400).json({ error: 'Invalid geolocation format' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  const getCalendarID = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventId = req.params.id; // Event ID
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'Schedule not found' });
        return;
      }
  
      // Find the event in the user's schedule based on the event ID
      const event = schedule.events.find(event => event.id === eventId);
  
      if (!event) {
        res.status(404).json({ error: 'Event not found in the schedule' });
        return;
      }
  
      // Assuming the event has a calendar ID property, you can access it like this
      const calendarID = event.calendarID;
  
      if (calendarID) {
        res.status(200).json({ calendarID: calendarID });
      } else {
        res.status(404).json({ error: 'Calendar ID not found for the event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
/*
  const editEventStartTime = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedStartTime = req.body.start; // Assuming the updated start time is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      schedule.events[eventIndex].timeFrame.start = updatedStartTime;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event start time updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event start time' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  
  const editEventEndTime = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
      const updatedEndTime = req.body.end; // Assuming the updated end time is sent in the request body
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ email: userEmail });
  
      if (!schedule) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      if (eventIndex < 0 || eventIndex >= schedule.events.length) {
        res.status(400).json({ error: 'Invalid event index' });
        return;
      }
  
      schedule.events[eventIndex].timeFrame.end = updatedEndTime;
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event end time updated successfully' });
      } else {
        res.status(500).json({ error: 'Failed to update event end time' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

  const deleteEventByID = async (req, res) => {
    try {
      const eventId = req.params.id; // Change 'email' to 'id'
  
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const schedule = await collection.findOne({ events: { $elemMatch: { id: eventId } } });
  
      if (!schedule) {
        res.status(404).json({ error: 'Event not found' });
        return;
      }
  
      const eventIndex = schedule.events.findIndex(event => event.id === eventId);
  
      if (eventIndex === -1) {
        res.status(400).json({ error: 'Event not found in user schedule' });
        return;
      }
  
      // Remove the event at the specified index
      schedule.events.splice(eventIndex, 1);
  
      const updateResult = await collection.updateOne(
        { _id: schedule._id },
        { $set: { events: schedule.events } }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'Event deleted successfully' });
      } else {
        res.status(500).json({ error: 'Failed to delete event' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  


  const deleteSchedule = async (req, res) => {
    try {
      const email = req.params.email; // Get the email from the URL parameter
  
      // Delete the user based on their email
      const collection = client.db('ScheduleDB').collection('schedulelist');
      const result = await collection.deleteOne({ email: email });
  
    if (result.deletedCount === 1) {
        res.status(200).json({ message: 'Schedule deleted successfully' });
      } else {
        res.status(404).json({ error: 'Schedule not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
  
  
  module.exports = {
    createNewSchedule,
    getScheduleByEmail,
    //insertEventAtIndex,
    //editEventName,
    //editEventAddress,
    //editEventDate,
    editEventGeolocation,
    //editEventStartTime,
    //editEventEndTime,
    //deleteEventAtIndex,
    deleteSchedule,
    //editEventAtIndex,
    updateSchedule,
    deleteEventByID,
    //editEventByID,
    addEvent,
    getCalendarID,





  };
  