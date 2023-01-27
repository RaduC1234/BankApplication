package me.raducapatina.server.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import me.raducapatina.server.command.core.Command;
import me.raducapatina.server.data.Article;
import me.raducapatina.server.data.ArticleService;
import me.raducapatina.server.data.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ArticleCommand extends Command {

    private static final Logger logger = LogManager.getLogger(ArticleCommand.class);

    public ArticleCommand() {
        this.name = "article";
        this.description = "Command adding articles in the main page.";
        this.usage = "article [delete/add] '<title>' || <file>";
    }

    @Override
    public void execute() throws Exception {
        String args[] = input.split(" ");

        switch (args[1]) {
            case "add" -> {
                ArticleService service = DatabaseManager.getInstance().getArticleService();
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                Article article = mapper.readValue(new File(args[2].replace("'", "")), Article.class);
                if(!new File(args[2].replace("'", "")).exists()) {
                    logger.error("File does not exist.");
                    return;
                }

                if(article.getImagePath() == null)
                    article.setImagePath("NULL");

                article.setDocumentPath(args[2]);
                service.add(article);
                logger.info("Article successfully added.");
            }
            case "delete" -> {
                ArticleService service = DatabaseManager.getInstance().getArticleService();
                String title = input.split("'")[1];
                if(service.deleteByTitle(title)) {
                    logger.info("Article successfully deleted.");
                }
            }
        }
    }
}
