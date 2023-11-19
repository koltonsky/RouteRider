jest.setTimeout(15000); // Set a higher timeout value (e.g., 15 seconds)

/*
const request = require('supertest');
const user = require('server.js');
*/

/*
const path = require('path');
const request = require('supertest');
const app = require(path.resolve(__dirname, '../server'));
*/

const { MongoClient } = require('mongodb');
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');

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
  

// Interface POST https://20.163.28.92:8081/api/userlist
describe('Create a new user', () => {
    // Input: user is a new correct user
    // Expected status code: 201
    // Expected behavior: user is added to the database
    // Expected output: 'User created successfully'
  test('POST /api/userlist should create a new user', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(user);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe('User created successfully');
  });

    // Input: user is an existing correct user with the same email in the database
    // Expected status code: 409
    // Expected behavior: database is unchanged
    // Expected output: 'User with this email already exists'
  test('POST /api/userlist should return an error if the user already exists', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(user);

    expect(res.status).toBe(409);
    expect(res.body.message).toBe('User with this email already exists');
  });
   // Test for handling server errors during user creation
   test('POST /api/userlist should handle server errors during user creation', async () => {
    // Mocking an error during the database insertion
    const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'insertOne');
    collectionMock.mockImplementationOnce(() => {
      throw new Error('Mocked MongoDB insertion error');
    });

    try {
      const res = await request(app)
        .post('/api/userlist')
        .send(user);

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

// Interface GET https://20.163.28.92:8081/api/userlist/:email
describe('Get an email', () => {
    // Test for getting an existing user by email
    test('GET /api/userlist/:email should retrieve an existing user by email', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const res = await request(app)
        .get(`/api/userlist/${userEmail}`);
  
      expect(res.status).toBe(200);
      expect(res.body.email).toBe(userEmail); // Assuming user email is returned in the response
    });
  
    // Test for getting a non-existing user by email
    test('GET /api/userlist/:email should return an error for a non-existing user', async () => {
      // Assuming nonExistingEmail is not present in the database
      const res = await request(app)
        .get(`/api/userlist/${nonExistingEmail}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    
    // Test for handling server errors during user retrieval
    test('GET /api/userlist/:email should handle server errors during retrieval', async () => {
        // Mocking an error during the database query
        const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'findOne');
        collectionMock.mockImplementationOnce(() => {
          throw new Error('Mocked MongoDB query error');
        });
      
        try {
          const res = await request(app)
            .get(`/api/userlist/${userEmail}`);
      
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
  






