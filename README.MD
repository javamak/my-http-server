./mvnw clean package && docker build -t myloadbalancer . && docker run --rm -p:9000:9000 --cpus=2 -m 2g  myloadbalancer:latest

docker run --rm -p:9000:9000 --cpus=2 -m 2g  myloadbalancer:latest

ab -c 350 -n 100000 http://localhost:9000/index.html 
