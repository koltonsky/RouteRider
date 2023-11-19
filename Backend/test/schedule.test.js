const { MongoClient } = require('mongodb');
/*
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');
*/
const supertest = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
const request = supertest(app);

const user = {
    "email": "newuserlol3@example.com",
    "name": "John Doe",
    "address": "123 Main St",
    "friends": [
      "friend1@example.com",
      "friend2@example.com"
    ],
    "friendRequests": [
      "friend3@example.com"
    ]
  };

const userEmail = 'koltonluu@gmail.com';
const nonExistingEmail = 'nonexistinguser@example.com';


  beforeAll(async () => {
    // Set up MongoDB connection before tests
    try {
      const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
      client = new MongoClient(uri);
      await client.connect();
      console.log("connected");
    } catch (error) {
      console.error('MongoDB Connection Error:', error);
    }
  });
  
  afterAll(async () => {
    // Close MongoDB connection after all tests
    if (client) {
      await client.close();
    }
  });
  
  beforeEach(() => {
    // Log messages or perform setup before each test if needed
    // Avoid logging directly in beforeAll for async operations
  });

  describe('Create New Schedule', () => {
    let scheduleData; // Declare scheduleData outside to use in different test cases
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      scheduleData = {
        email: 'user@example.com',
        // Add other schedule data properties as needed
      };
    });
  
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteOne({ email: scheduleData.email });
    });
  
    // Test for successfully creating a new schedule
    test('POST /api/schedulelist should create a new schedule successfully', async () => {
      const res = await request
        .post('/api/schedulelist')
        .send(scheduleData);
  
      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Schedule created successfully');
    });
  
    // Test for creating a schedule with an existing email
    test('POST /api/schedulelist should return an error for an existing schedule', async () => {
      // Assuming a schedule with the same email already exists
      await request
        .post('/api/schedulelist')
        .send(scheduleData);
  
      const res = await request
        .post('/api/schedulelist')
        .send(scheduleData);
  
      expect(res.status).toBe(409);
      expect(res.body.message).toBe('Schedule with this email already exists');
    });
  
    // Test for handling server errors during schedule creation
    test('POST /api/schedulelist should handle server errors during creation', async () => {
      // Mocking an error during the database insertion
      const collectionMock = jest.spyOn(client.db('ScheduleDB').collection('schedulelist'), 'insertOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB insertion error');
      });
  
      try {
        const res = await request
          .post('/api/schedulelist')
          .send(scheduleData);
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });
  
  
  
describe('GET /api/schedulelist/:email', () => {
    // Mock schedule data for testing
    const scheduleData = {
      email: 'test@example.com',
      // ... other schedule properties
    };
  
    // Before running the tests, add a sample schedule to the database
    beforeAll(async () => {
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.insertOne(scheduleData);
    });
  
    // After running the tests, remove the sample schedule from the database
    afterAll(async () => {
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteOne({ email: scheduleData.email });
    });
  
    test('should return schedule when it exists', async () => {
      const res = await request.get(`/api/schedulelist/${scheduleData.email}`);
  
      expect(res.status).toBe(200);
      expect(res.body).toBe('Schedule found');
    });
  
    test('should return 404 if schedule does not exist', async () => {
      const nonExistingEmail = 'nonexistent@example.com';
      const res = await request.get(`/api/schedulelist/${nonExistingEmail}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Schedule not found');
    });
  
    test('GET /api/schedulelist/:email should return 500 on internal server error', async () => {
        // Mock an internal server error by not connecting to the MongoDB client
        jest.spyOn(client, 'db').mockImplementationOnce(() => {
          throw new Error('Mocked MongoDB connection error');
        });
      
        try {
          const res = await request.get(`/api/schedulelist/${scheduleData.email}`);
      
          expect(res.status).toBe(500);
          expect(res.body.error).toBe('Internal server error');
        } catch (error) {
          console.error('Test error:', error);
        } finally {
          // Restore the original implementation of the mocked method
          jest.restoreAllMocks();
        }
      });      
  });

  describe('GET /api/schedulelist/:email/:id', () => {
    const userEmail = 'user@example.com';
    const eventId = '12345'; // Event ID
  
    // Mock schedule data for testing
    const scheduleData = {
      email: userEmail,
      events: [
        {
          id: eventId,
          calendarID: 'calendar123',
          // ... other event properties
        },
      ],
      // ... other schedule properties
    };
  
    // Before running the tests, add a sample schedule to the database
    beforeAll(async () => {
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.insertOne(scheduleData);
    });
  
    // After running the tests, remove the sample schedule from the database
    afterAll(async () => {
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteOne({ email: scheduleData.email });
    });
  
    test('should return calendarID when both schedule and event are found', async () => {
      const res = await request.get(`/api/schedulelist/${userEmail}/${eventId}`);
  
      expect(res.status).toBe(200);
      expect(res.body.calendarID).toBe('calendar123');
    });
  
    test('should return 404 when schedule is not found', async () => {
      const nonExistingEmail = 'nonexistent@example.com';
      const res = await request.get(`/api/schedulelist/${nonExistingEmail}/${eventId}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Schedule not found');
    });
  
    test('should return 404 when event is not found in the schedule', async () => {
      const nonExistingEventId = 'nonexistentEvent';
      const res = await request.get(`/api/schedulelist/${userEmail}/${nonExistingEventId}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Event not found in the schedule');
    });
  
    test('should return 404 when calendarID is not found for the event', async () => {
      const eventWithoutCalendarID = { id: 'noCalendarIDEvent' };
      const modifiedSchedule = { ...scheduleData, events: [eventWithoutCalendarID] };
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.updateOne({ email: userEmail }, { $set: modifiedSchedule });
  
      const res = await request.get(`/api/schedulelist/${userEmail}/${eventWithoutCalendarID.id}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Calendar ID not found for the event');
    });
  
    test('should return 500 on internal server error', async () => {
        // Mock an internal server error by not connecting to the MongoDB client
        jest.spyOn(client, 'db').mockImplementationOnce(() => {
          throw new Error('Mocked MongoDB connection error');
        });
      
        try {
          const res = await request.get(`/api/schedulelist/${userEmail}/${eventId}`);
      
          expect(res.status).toBe(500);
          expect(res.body.error).toBe('Internal server error');
        } catch (error) {
          console.error('Test error:', error);
        } finally {
          // Restore the original implementation of the mocked method
          jest.restoreAllMocks();
        }
      });
      
  });
  
  describe('POST /api/schedulelist/:email', () => {
    // Mock user data for testing
    const scheduleData = {
      email: 'test@example.com',
      events: [],
      // ... other user properties
    };
  
    // Mock event data for testing
    const newEventData = {
      eventName: 'Test Event',
      startTime: '2023-10-30T15:00:00.000-07:00',
      endTime: '2023-10-30T17:00:00.000-07:00',
      // ... other event properties
    };
  
    // Before running the tests, add a sample user to the database
    beforeAll(async () => {
        const collection = client.db('ScheduleDB').collection('schedulelist');
        await collection.insertOne(scheduleData);
      });
    
      // After running the tests, remove the sample schedule from the database
      afterAll(async () => {
        const collection = client.db('ScheduleDB').collection('schedulelist');
        await collection.deleteOne({ email: scheduleData.email });
      });
  
    test('should add a new event successfully', async () => {
      const res = await request
        .post(`/api/schedulelist/${scheduleData.email}`)
        .send(newEventData);
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Event added successfully');
    });
  
    test('should return 404 for a non-existing user', async () => {
      const nonExistingEmail = 'nonexistent@example.com';
      const res = await request
        .post(`/api/schedulelist/${nonExistingEmail}`)
        .send(newEventData);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Schedule not found');
    });
  
    test('should return 400 for invalid startTime or endTime', async () => {
      const invalidEventData = {
        ...newEventData,
        startTime: 'invalid-date',
      };
  
      const res = await request
        .post(`/api/schedulelist/${scheduleData.email}`)
        .send(invalidEventData);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid startTime or endTime');
    });
  
    test('should return 400 for event duration less than or equal to 0', async () => {
      const invalidEventData = {
        ...newEventData,
        endTime: newEventData.startTime,
      };
  
      const res = await request
        .post(`/api/schedulelist/${scheduleData.email}`)
        .send(invalidEventData);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Event duration must be greater than 0');
    });
  
    test('should return 400 for overlapping events', async () => {
        console.log("RUNNING");
      // Assuming there's an existing event in the user's schedule
      const existingEvent = {
        title: 'Existing Event',
        startTime: '2023-10-31T16:00:00.000-07:00',
        endTime: '2023-10-31T18:00:00.000-07:00',
        // ... other event properties
      };
  
      await request
        .post(`/api/schedulelist/${scheduleData.email}`)
        .send(existingEvent);
  
      // Attempting to add a new event that overlaps with the existing one
      const overlappingEventData = {
        title: 'Overlapping Event',
        startTime: '2023-10-31T17:00:00.000-07:00',
        endTime: '2023-10-31T19:00:00.000-07:00',
        // ... other event properties
      };
  
      const res = await request
        .post(`/api/schedulelist/${scheduleData.email}`)
        .send(overlappingEventData);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Event overlaps with existing events');
    });
  
    test('should return 500 on internal server error', async () => {
        // Mock an internal server error by not connecting to the MongoDB client
        jest.spyOn(client, 'db').mockImplementationOnce(() => {
          throw new Error('Mocked MongoDB connection error');
        });
      
        try {
          const res = await request
            .post(`/api/schedulelist/${scheduleData.email}`)
            .send(newEventData);
      
          expect(res.status).toBe(500);
          expect(res.body.error).toBe('Internal server error');
        } catch (error) {
          console.error('Test error:', error);
        } finally {
          // Restore the original implementation of the mocked method
          jest.restoreAllMocks();
        }
      });
      
  });


describe('DELETE /api/schedulelist/:email/:id', () => {
  // Mock schedule data for testing
  const scheduleData = {
    email: 'user@example.com',
    events: [
      { id: 'event1', /* other event properties */ },
      { id: 'event2', /* other event properties */ },
    ],
  };

  // Before running the tests, add a sample schedule to the database
  beforeAll(async () => {
    const collection = client.db('ScheduleDB').collection('schedulelist');
    await collection.insertOne(scheduleData);
  });

  // After running the tests, remove the sample schedule from the database
  afterAll(async () => {
    const collection = client.db('ScheduleDB').collection('schedulelist');
    await collection.deleteOne({ email: scheduleData.email });
  });

  test('should delete an existing event and return 200', async () => {
    const eventIdToDelete = 'event1';
    const res = await request.delete(`/api/schedulelist/${scheduleData.email}/${eventIdToDelete}`);

    expect(res.status).toBe(200);
    expect(res.body.message).toBe('Event deleted successfully');

    // Check if the event is actually deleted from the schedule in the database
    const collection = client.db('ScheduleDB').collection('schedulelist');
    const updatedSchedule = await collection.findOne({ email: scheduleData.email });
    const deletedEvent = updatedSchedule.events.find(event => event.id === eventIdToDelete);
    expect(deletedEvent).toBeUndefined();
  });

  test('should return 404 for a non-existing event', async () => {
    const nonExistingEventId = 'nonexistentEvent';
    const res = await request.delete(`/api/schedulelist/${scheduleData.email}/${nonExistingEventId}`);

    expect(res.status).toBe(404);
    expect(res.body.error).toBe('Event not found');
  });

  test('should return 400 if event not found in user schedule', async () => {
    const eventIdNotInUserSchedule = 'eventNotInUserSchedule';
    const res = await request.delete(`/api/schedulelist/${scheduleData.email}/${eventIdNotInUserSchedule}`);

    expect(res.status).toBe(404);
    expect(res.body.error).toBe('Event not found');
  });

  test('should return 500 on internal server error', async () => {
    // Mock an internal server error by not connecting to the MongoDB client
    jest.spyOn(client, 'db').mockImplementationOnce(() => {
      throw new Error('Mocked MongoDB connection error');
    });
  
    try {
      const eventIdToDelete = 'event2';
      const res = await request.delete(`/api/schedulelist/${scheduleData.email}/${eventIdToDelete}`);
  
      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Internal server error');
    } catch (error) {
      console.error('Test error:', error);
    } finally {
      // Restore the original implementation of the mocked method
      jest.restoreAllMocks();
    }
  });
  
});
  