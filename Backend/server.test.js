const { MongoClient } = require('mongodb');
/*
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');
*/
const supertest = require('supertest');
const { app } = require('./server.js'); // Replace with the actual path to your Express app
const request = supertest(app);

const { findUserToken } = require('./server');
const mockSendNotification = jest.fn(() => 1);

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

// Interface POST /api/store_token
describe('Create/update firebasetokens for mobile notifications', () => {
    const userData = {
        email: 'user@example.com',
        token: '1234567890'
    };
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      const collection = client.db('UserDB').collection('userlist');
      await collection.insertOne(userData);
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: userData.email });
    });

    // Input: req.body.email matches a user in the database and req.body.token is not null
    // Expected status code: 200
    // Expected behavior: database is updated with the new token 
    test('Matching email and valid token', async () => {
        const response = await request.post('/api/store_token').send({
            email: 'user@example.com', 
            token: '54321'
        });
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({message: 'fcmToken updated successfully'});
    });

    // Input: req.body.email does not match a user in the database and req.body.token is not null
    // Expected status code: 400
    // Expected behavior: database is unchanged 
    test('Non-existing email and valid token', async () => {
        const response = await request.post('/api/store_token').send({
            email: 'user1@example.com', 
            token: '54321'
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'User not found, failed to update fcmToken'});
    });

    // Input: req.body.email and req.body.token are null
    // Expected status code: 400
    // Expected behavior: database is unchanged 
    test('Null email and token', async () => {
        const response = await request.post('/api/store_token').send({
            email: null, 
            token: null
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'Invalid email or token'});
    });
});

// Interface POST /api/send-friend-notification
describe('Sending friend request notifications', () => {
    const senderName = 'sender name';
    const receiverData = {
        email: 'receiver@example.com',
        fcmToken: '0987654321'
    };

    // Set up the test data before running the tests
    beforeAll(async () => {
      const collection = client.db('UserDB').collection('userlist');
      await collection.insertOne(receiverData);
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: receiverData.email });
    });

    // Input: provided email does not match a user in the database
    // Expected status code: 400
    // Expected behavior: notification is not sent and an error message is returned
    test('matching user not found', async () => {
        const response = await findUserToken("non-exist@emample.com", senderName, mockSendNotification);
        expect(mockSendNotification).not.toHaveBeenCalled();
        expect(response.status).toBe(400);
        expect(response.message).toEqual('Receiver not found, failed to send notification');
    });

    // Input: provided email matches a user in the database
    // Expected status code: 200
    // Expected behavior: a friend request notification is sent to the specified user and a success message is returned
    test('matching user found', async () => {
        const response = await findUserToken(receiverData.email, senderName, mockSendNotification);
        expect(mockSendNotification).toHaveBeenCalled();
        expect(response.status).toBe(200);
        expect(response.message).toEqual('Successfully sent friend request notification');
    });
});



