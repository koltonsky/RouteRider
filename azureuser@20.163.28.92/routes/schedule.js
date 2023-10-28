
const {MongoClient, ObjectId} = require('mongodb');
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

const createNewSchedule = async (req, res) => {
    try {
      // Extract schedule data from the request body
      const schedule = req.body;

      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
  
      // Insert the new user document into the collection
      const insertResult = await collection.insertOne(schedule);
  
      res.status(201).json({ message: 'Schedule created successfully' });

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
  
  module.exports = {
    createNewSchedule,
    getScheduleByEmail,
  };
  