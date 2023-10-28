
const {MongoClient, ObjectId} = require('mongodb');
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

const createNewSchedule = async (req, res) => {
  try {
    // Extract schedule data from the request body
    const schedule = req.body;

    // Assuming you have already connected to the MongoDB client
    const collection = client.db('ScheduleDB').collection('schedulelist');

    // Check if a user with a specific identifier (e.g., email) already exists
    const existingUser = await collection.findOne({ email: schedule.email });
    
    if (existingUser) {
      // If a user with the same email exists, return an error message
      res.status(100).json({ message: 'User with this email already exists'});
    } else {
      // If the user doesn't exist, insert the new schedule document into the collection
      const insertResult = await collection.insertOne(schedule);
      res.status(201).json({ message: 'Schedule created successfully' });
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
        res.status(200).json(schedule);
        
      } else {
        // User not found
        res.status(404).json({ error: 'User not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  



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

  const deleteEventAtIndex = async (req, res) => {
    try {
      const userEmail = req.params.email;
      const eventIndex = parseInt(req.params.index);
  
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
  
  
  
  module.exports = {
    createNewSchedule,
    getScheduleByEmail,
    insertEventAtIndex,
    editEventName,
    editEventAddress,
    editEventDate,
    editEventGeolocation,
    editEventStartTime,
    editEventEndTime,
    deleteEventAtIndex




  };
  