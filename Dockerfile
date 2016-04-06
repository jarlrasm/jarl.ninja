FROM nginx
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/bin/lein && chmod 755 /usr/bin/lein
ENV LEIN_ROOT=1
RUN lein
COPY . /app
RUN cd app && lein cljsbuild once && cp -r public/resources /usr/share/nginx/html
