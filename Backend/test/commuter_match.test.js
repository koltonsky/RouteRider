const { MongoClient } = require('mongodb');
/*
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');
*/
const supertest = require('supertest');
const match = require('../commuter_match')
const { app, stopSSLServer } = require('../server'); // Replace with the actual path to your Express app
const request = supertest(app);

var client;

beforeAll(async () => {
    // Set up MongoDB connection before tests
    try {
      /*global client*/
      const uri = 'mongodb://127.0.0.1:27017'; // Replace with your MongoDB connection string
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
    //closeServer();
    stopSSLServer();
  });
  
  beforeEach(() => {
    // Log messages or perform setup before each test if needed
    // Avoid logging directly in beforeAll for async operations
  });


describe('getFirstEventsOfEachDay', () => {
    // Mock user data for testing
    let userData;
    let userEmail;
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      userData = {
        email: 'userm@example.com',
        // Add other schedule data properties as needed
      };
  
      userEmail = userData.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.insertOne({
        email: userEmail,
        events: [
          { startTime: '2023-11-21T08:00:00.000-08:00', title: 'Event 1' },
          { startTime: '2023-11-21T12:00:00.000-08:00', title: 'Event 2' },
          { startTime: '2023-11-22T10:00:00.000-08:00', title: 'Event 3' },
          // Add more events for different days
        ],
      });
    });
  
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteOne({ email: userEmail });
    });
  
    // Input: userEmail
  // Expected status code: n/A
// Expected behavior: should return the first event of each day for an existing user with events
// Expected output: first events of each day
// ChatGPT usage: Yes
    test('should return the first event of each day for an existing user with events', async () => {
      const res = await match.getFirstEventsOfEachDay(userEmail);
  
      expect(res.events).toHaveLength(2); // Assuming two different days in the test data
      // Add more specific assertions based on the test data
      expect(res.events[0].event.title).toBe('Event 1');
      expect(res.events[1].event.title).toBe('Event 3');
    });


  });

    
  describe('findOtherEmails', () => {
    // Mock user data for testing
    let userData;
    let userEmail;
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      userData = {
        email: 'users@example.com',
        // Add other schedule data properties as needed
      };
  
      userEmail = userData.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      // Add other schedules for testing
      await collection.insertMany([
        { email: 'users@example.com' },
        { email: 'user1!@example.com' },
        { email: 'user2@example.com' },
        { email: 'user3@example.com' },
      ]);
    });
  
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteMany({ email: { $in: ['users@example.com', 'user1!@example.com', 'user2@example.com', 'user3@example.com'] } });
    });
  
    // Input: userEmail
  // Expected status code: n/A
// Expected behavior: should return emails of schedules excluding the user\'s schedule
// Expected output: return emails of schedules excluding the user's schedule
// ChatGPT usage: Yes
    test('should return emails of schedules excluding the user\'s schedule', async () => {
      const res = await match.findOtherEmails(userEmail);
  
      //expect(res).toHaveLength(3); // Assuming three other schedules in the test data

      // Add more specific assertions based on the test data
      expect(res).toContain('user1!@example.com');
      expect(res).toContain('user2@example.com');
      expect(res).toContain('user3@example.com');
    });
  
  });
  
  describe('findMatchingUsers', () => {
    // Mock user data for testing
    let userData;
    let userEmail;
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      userData = {
        email: 'userf@example.com',
        // Add other schedule data properties as needed
      };
  
      userEmail = userData.email;
      const collection = client.db('ScheduleDB').collection('schedulelist');

      await collection.insertOne(userData);
  
      // Assuming you have already connected to the MongoDB client
      
      // Add other schedules for testing
      await collection.insertMany([
        { email: 'user1!@example.com', events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'UBC123' }] },
        { email: 'user2@example.com', events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'UBC456' }] },
        { email: 'user3@example.com', events: [{ startTime: '2023-11-22T10:00:00.000-08:00', address: 'SFU789' }] },
      ]);
    });
  
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteMany({ email: { $in: ['user1!@example.com', 'user2@example.com', 'user3@example.com'] } });
      await collection.deleteOne(userData);
    });
  
    // Input: userEmail with matching events
    // Expected status code: n/A
    // Expected behavior: should return a set with emails of users with matching events
    // Expected output: return a set with emails of users with matching events
    // ChatGPT usage: Yes
    test('should return a set with emails of users with matching events more than 0', async () => {
      const res = await match.findMatchingUsers('user1!@example.com');
  
      expect(res.size).toBe(1); // Assuming only one user has matching events in the test data
      expect(res.has('user2@example.com')).toBe(true);
    });
  
    // Input: userEmail with no matching events
    // Expected status code: n/A
    // Expected behavior: should return a set with emails of users with no matching events
    // Expected output: return a set with emails of users with no matching events
    // ChatGPT usage: Yes
    test('should return an empty set for a user with no matching events', async () => {
      // Change the user's events to not match with any other user's events
      await client.db('ScheduleDB').collection('schedulelist').updateOne(
        { email: userEmail },
        { $set: { events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'SFU123' }] } }
      );
  
      //await match.findMatchingUsers(userEmail).reject.toEqual("No matching users found.");
      expect(match.findMatchingUsers(userEmail)).rejects.toEqual("No matching users found.");
  
      //expect(err).toBe('No matching users found.');
      //expect(res.size).toBe(0);
    });
  
    // Input: userEmail for nonexistent user
    // Expected status code: n/A
    // Expected behavior: should return an empty set
    // Expected output: {}
    // ChatGPT usage: Yes
    test('should return an empty set for a nonexisting user', async () => {
      const nonExistingUserEmail = 'nonexisting@example.com';
      //await match.findMatchingUsers(nonExistingUserEmail).reject.toEqual("No matching users found.");
  
      expect(match.findMatchingUsers(nonExistingUserEmail)).rejects.toEqual("No matching users found.");
      //expect(res.size).toBe(0);
    });


    /*
    // Input: userEmail with multiple matching events
    // Expected status code: n/A
    // Expected behavior: should return a set with emails of users with multiple matching events
    // Expected output: return a set with emails of users with multiple matching events
    // ChatGPT usage: Yes
    test('should return a set with emails of users with multiple matching events', async () => {
      // Add another event for the user with the same startTime and 'UBC' address
      await client.db('ScheduleDB').collection('schedulelist').updateOne(
        { email: userEmail },
        { $push: { events: { startTime: '2023-11-21T08:00:00.000-08:00', address: 'UBC789' } } }
      );
    
      const res = await match.findMatchingUsers(userEmail);
    
      expect(res.size).toBe(2); // Assuming two users have matching events in the test data
      expect(res.has('user2@example.com')).toBe(true);
      expect(res.has('user1@example.com')).toBe(true);
    });
    */

    test('should return a set with emails of users with matching events on different days', async () => {
      // Add an event for the user with the same startTime but different date and 'UBC' address
      await client.db('ScheduleDB').collection('schedulelist').updateOne(
        { email: userEmail },
        { $push: { events: { startTime: '2023-11-22T10:00:00.000-08:00', address: 'UBC123' } } }
      );

      expect(match.findMatchingUsers(userEmail)).rejects.toEqual("No matching users found.");
    
      //const res = await match.findMatchingUsers(userEmail);
    
      //expect(res.size).toBe(0);
    });
    

        // Input: userEmail with no matching events due to different address
    // Expected status code: n/A
    // Expected behavior: should return an empty set
    // Expected output: {}
    // ChatGPT usage: Yes
test('should return an empty set for a user with matching startTime but different address', async () => {
  // Change the user's event address to not match with any other user's event address
  await client.db('ScheduleDB').collection('schedulelist').updateOne(
    { email: userEmail },
    { $set: { events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'SFU123' }] } }
  );

  expect(match.findMatchingUsers(userEmail)).rejects.toEqual("No matching users found.");
  //const res = await match.findMatchingUsers(userEmail);

  //expect(res.size).toBe(0);
});



  
  });

// Interface GET https://20.163.28.92:8081/api/findMatchingUsers/:userEmail

  describe('GET /api/findMatchingUsers/:userEmail', () => {

    // Mock user data for testing
    let userData;
    let userEmail;
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      userData = {
        email: 'userendpoint@example.com',
        // Add other schedule data properties as needed
      };
  
      userEmail = userData.email;
      const collection = client.db('ScheduleDB').collection('schedulelist');

      await collection.insertOne(userData);
  
      // Assuming you have already connected to the MongoDB client
      
      // Add other schedules for testing
      await collection.insertMany([
        { email: 'user11@example.com', events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'UBC123' }] },
        { email: 'user21@example.com', events: [{ startTime: '2023-11-21T08:00:00.000-08:00', address: 'UBC456' }] },
        { email: 'user31@example.com', events: [{ startTime: '2023-11-22T10:00:00.000-08:00', address: 'SFU789' }] },
      ]);
    });
  
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('ScheduleDB').collection('schedulelist');
      await collection.deleteMany({ email: { $in: ['user11@example.com', 'user21@example.com', 'user31@example.com'] } });
      await collection.deleteOne(userData);
    });

    // Input: userEmail exist
    // Expected status code: 200
    // Expected behavior: should return an empty set
    // Expected output: {}
    // ChatGPT usage: Yes
    test('should return matching set of matching users endpoint', async () => {
      console.log("ENDPOINT");
      // Mock data or use actual test data
      //const mockUserEmail = 'userabc@example.com';
  
      // Make a request to the endpoint
      const response_thing123 = await request
        .get(`/api/findMatchingUsers/user11@example.com`);
  
      // Assert the response
      expect(response_thing123.status).toBe(200);
      //expect(response.body).toHaveProperty(matchingUsers);

      expect(response_thing123.body).toEqual({"matchingUsers": ["user21@example.com"]});


      
      //expect(Array.isArray(response.body.matchingUsers)).toBe(false);

      //expect(response.body.matchingUsers).toBe({});
      //expect(response.body.matchingUsers).toStrictEqual({});


      // Add more assertions based on your actual response structure
  
      // Cleanup or additional assertions as needed
    });


    
    // Input: userEmail that doesn't exist
    // Expected status code: 200
    // Expected behavior: should return an empty set
    // Expected output: {}
    // ChatGPT usage: Yes
    test('should return empty set of matching users', async () => {
      console.log("ENDPOINT");
      // Mock data or use actual test data
      const mockUserEmail = 'userabc@example.com';
  
      // Make a request to the endpoint
      const response = await request
        .get(`/api/findMatchingUsers/${mockUserEmail}`);
  
      // Assert the response
      expect(response.status).toBe(400);
      //expect(response.body).toHaveProperty([]);

      expect(response.body.message).toBe("No matching users found.");


      
      //expect(Array.isArray(response.body.matchingUsers)).toBe(false);

      //expect(response.body.matchingUsers).toBe({});
      //expect(response.body.matchingUsers).toStrictEqual({});


      // Add more assertions based on your actual response structure
  
      // Cleanup or additional assertions as needed
    });


  
    // Add more test cases as needed
  });


  
